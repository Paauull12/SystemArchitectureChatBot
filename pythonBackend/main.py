from model import RAG

rag = RAG(
    docs_dir='fakeDocs', 
    n_retrievals=1,
    chat_max_tokens=3097,  
    creativeness=1.2,
)

while True:
    question = str(input("Intrebare: "))
    if question == "stop":
        break
    answer = rag.ask(question)
    print('Resposta:', answer)