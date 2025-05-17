from flask import Flask, request, jsonify
from flask_cors import CORS
from model import RAG

app = Flask(__name__)
CORS(app)

rag_instance = None
rag_instance_decision = None

def get_rag():
    global rag_instance
    if rag_instance is None:
        rag_instance = RAG(
            docs_dir='fakeDocs', 
            n_retrievals=4,
            model_name="gpt-4.1-nano",
            creativeness=0.2,
            context_from_milvus = True
        )
    return rag_instance

def get_rag_decision():
    global rag_instance_decision
    if rag_instance_decision is None:
        rag_instance_decision = RAG(
            docs_dir = 'fakeDocs',
            model_name = "gpt-4.1-nano",
            creativeness = 0.0,
            system_prompt = "You are an experienced java engineer and you job will be to pick\n" \
            "between more files with some metrics.\n" \
            "The files will be listed following the format\n" \
            "File Path: [filePath]\n" \
            "File Metrics: [listOfFileMetrics]\n" \
            "Based on these your job is to find the files with the most problmes in your architecutre\n" \
            "Here you have every metric explain in detail\n" \
            "" \
            "Make your choice and make sure that you return the filePath of every file\n" \
            "Return at most 1/4 of the total number of files send to you if the number of files is > 10 else return as many as you would like.\n" \
            "You should output filePath filePath so nothing else aside from filePath of the file and space\n",
        )
    return rag_instance_decision

@app.route('/api/chat', methods=['POST'])
def chat():
    try:
        rag_instance = get_rag()
        
        data = request.get_json()
        
        user_input = data.get('text')

        answer = rag_instance.ask(user_input)

        return jsonify(
            {'message': answer},
        ), 200
    except Exception as e:
        print(e)
        return jsonify(
            {'error': str(e)},
        ), 500

@app.route('/api/getfilessmart', methods=['POST'])
def retrieveFilesSmart():
    try:
        rag_instance_decision = get_rag_decision()
        
        data = request.get_json()
        
        user_input = data.get('text')

        answer = rag_instance_decision.ask(user_input)

        list_of_files = answer.split()

        return jsonify(
            {'message': list_of_files}  
        ), 200

    except Exception as e:
        print(e)
        return jsonify(
            {'error': str(e)},
        ), 500

if __name__ == '__main__':
    app.run(debug=True)