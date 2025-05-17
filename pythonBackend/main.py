from model import RAG

rag = RAG(
            docs_dir='fakeDocs', 
            n_retrievals=4,
            model_name="gpt-4.1-nano",
            creativeness=0.2,
        )

while True:
    question = str(input("Intrebare: "))
    if question == "stop":
        break
    answer = rag.ask(question)
    print('Resposta:', answer)