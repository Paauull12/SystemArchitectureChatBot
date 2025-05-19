import os
from dotenv import load_dotenv
load_dotenv()

from typing import Dict
from langchain.retrievers.self_query.base import SelfQueryRetriever
from langchain.chains.query_constructor.base import AttributeInfo
from langchain.prompts import ChatPromptTemplate, MessagesPlaceholder
from langchain.schema.output_parser import StrOutputParser
from langchain_core.runnables.history import RunnableWithMessageHistory
from langchain_core.chat_history import BaseChatMessageHistory
from langchain_core.messages import BaseMessage, AIMessage
from langchain_openai.embeddings import OpenAIEmbeddings
from langchain_openai.chat_models import ChatOpenAI
from langchain_community.document_loaders import DirectoryLoader
from langchain_community.vectorstores import Milvus
from milvus import default_server as milvus_server

class InMemoryHistory(BaseChatMessageHistory):
    def __init__(self):
        self.messages: list[BaseMessage] = []

    def add_messages(self, messages: list[BaseMessage]) -> None:
        self.messages.extend(messages)

    def clear(self) -> None:
        self.messages = []

store: Dict[str, InMemoryHistory] = {}

def get_by_session_id(session_id: str) -> BaseChatMessageHistory:
    if session_id not in store:
        store[session_id] = InMemoryHistory()
    return store[session_id]

class RAG():
    def __init__(self,
                 docs_dir: str,
                 n_retrievals: int = 4,
                 model_name = "gpt-4.1-nano",
                 creativeness: float = 0.7,
                 system_prompt: str = "",
                 context_from_milvus = False
                ):

        if system_prompt == "":
            self.__system_prompt = (
                "You are a seasoned software architect with over 10 years of experience in Java-based enterprise systems. "
                "Provided below are code snippets and associated metrics extracted from our project. Please perform a comprehensive analysis to identify potential architectural design flaws, including but not limited to:\n\n"
                "- Tight coupling between components\n"
                "- Violation of SOLID principles\n"
                "- Scalability bottlenecks\n"
                "- Single points of failure\n"
                "- Redundancies or unnecessary complexities\n"
                "- Security vulnerabilities\n"
                "- Inappropriate use of architectural patterns\n\n"
                "For each identified issue, provide:\n\n"
                "- A clear description of the flaw\n"
                "- The potential impact on system performance, maintainability, or scalability\n"
                "- Recommendations for improvement or refactoring\n\n"
                "Please structure your response with clear headings for each identified flaw."
                "Keep your answer short and on try to best respond to the user's needs"
                "Respond as best as possible to the uses question if it is realted to code or ignore it otherwise"
                "If the question is related to code but not to architecture respond to it and make short suggestion about architecture"
                "Keep your answer short and use a normal language."
            )
        else:
            self.__system_prompt = system_prompt

        self.__context_from_milvus = context_from_milvus
        self.__model = self.__set_llm_model(model_name, creativeness)
        self.__docs_list = self.__get_docs_list(docs_dir)
        if self.__context_from_milvus:
            print("we are here from another")
            self.__retriever = self.__set_retriever(k=n_retrievals)
        self.__chain = self.__build_chain()

    def __set_llm_model(self, model_name="gpt-4.1-nano", temperature: float = 0.7):
        return ChatOpenAI(model_name=model_name, temperature=temperature)

    def __get_docs_list(self, docs_dir: str) -> list:
        print("Incarcam documente: ")

        loader = DirectoryLoader(
            docs_dir,
            recursive=True,
            show_progress=True,
            use_multithreading=True,
            max_concurrency=4
        )
        docs_list = loader.load_and_split()
        return docs_list

    def __set_retriever(self, k: int = 4):
        embeddings = OpenAIEmbeddings()
        milvus_server.start()

        vector_store = Milvus.from_documents(
            self.__docs_list,
            embedding=embeddings,
            connection_args={"host": os.getenv("MILVUS_HOST"), "port": os.getenv("MILVUS_PORT")},
            collection_name="architecture_examples"
        )

        metadate_field_info = [
            AttributeInfo(
                name="source",
                description="Calea directorului unde se afla documentul",
                type="string",
            ),
        ]

        document_content_descrition = "Architecture documents"

        _retriever = SelfQueryRetriever.from_llm(
            self.__model,
            vector_store,
            document_content_descrition,
            metadate_field_info,
            search_kwargs={"k": k}
        )

        return _retriever

    def __build_chain(self):
        prompt = ChatPromptTemplate.from_messages([
            ("system", self.__system_prompt),
            MessagesPlaceholder(variable_name="chat_history"),
            ("user", "{input}")
        ])

        output_parser = StrOutputParser()

        chain = prompt | self.__model | output_parser

        chain_with_history = RunnableWithMessageHistory(
            chain,
            get_by_session_id,
            input_messages_key="input",
            history_messages_key="chat_history"
        )

        return chain_with_history

    def ask(self, question: str, session_id: str = "default") -> str:
        try:
            input_payload = {
                "input": question
            }

            if self.__context_from_milvus:
                input_payload["context"] = self.__retriever.invoke(question)

            answer = self.__chain.invoke(
                input_payload,
                config={"configurable": {"session_id": session_id}}
            )

            return answer
        except Exception as e:
            print(f"Error in RAG.ask(): {str(e)}")
            return f"I'm sorry, I encountered an error: {str(e)}"
