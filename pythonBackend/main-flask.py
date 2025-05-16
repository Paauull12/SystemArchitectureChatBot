from flask import Flask, request, jsonify
from flask_cors import CORS
from model import RAG

app = Flask(__name__)
CORS(app)

rag_instance = None

def get_rag():
    global rag_instance
    if rag_instance is None:
        rag_instance = RAG(
            docs_dir='fakeDocs', 
            n_retrievals=1,
            chat_max_tokens=3097,  
            creativeness=1.2,
        )
    return rag_instance

@app.route('/api/chat', methods=['POST'])
def chat():
    data = request.get_json()
    
    return jsonify(
        {'message': "we hit an endpoint"},
    ), 200


if __name__ == '__main__':
    app.run(debug=True)