const vscode = acquireVsCodeApi();
const messagesContainer = document.getElementById('messages');
const messageInput = document.getElementById('messageInput');
const sendButton = document.getElementById('sendButton');
const chooseFileNormal = document.getElementById('chooseFileNormal');
const clearButton = document.getElementById('clearButton');
const chooseFileFancy = document.getElementById('chooseFileFancy');
vscode.postMessage({ command: 'initialized' });

function sendMessage() {
  const text = messageInput.value;

  if(chooseFileNormal.checked) {
    if(text !== ''){
      vscode.postMessage({command: 'chooseFileNormal', text});
      chooseFileNormal.checked = false;
      messageInput.value = '';
    }
    return;
  }else if(chooseFileFancy.checked){
    if(text !== ''){
      vscode.postMessage({command: 'chooseFileFancy', text});
      chooseFileFancy.checked = false;
      messageInput.value = '';
    }
    return;
  }

  if (text.trim() === '') {return;}
  vscode.postMessage({ command: 'sendMessage', text });
  messageInput.value = '';
}

function escapeHtml(text) {
  return text.replace(/&/g, "&amp;")
             .replace(/</g, "&lt;")
             .replace(/>/g, "&gt;")
             .replace(/"/g, "&quot;")
             .replace(/'/g, "&#039;");
}

function addMessageToUI(message) {
  const msgEl = document.createElement('div');
  msgEl.className = 'message ' + message.sender;
  
  if (message.isCode) {
    // Conținut cod - afișează ca HTML escaped
    msgEl.innerHTML = '<pre><code>' + escapeHtml(message.text) + '</code></pre>';
  } else if (message.sender === 'bot') {
    // Pentru mesajele de la bot, convertim Markdown în HTML
    // Folosim marked.parse() pentru a converti Markdown în HTML
    msgEl.innerHTML = marked.parse(message.text);
  } else {
    // Pentru mesajele utilizatorului, păstrăm formatul simplu
    msgEl.innerHTML = escapeHtml(message.text).replace(/\n/g, '<br>');
  }
  
  const ts = document.createElement('div');
  ts.className = 'time-stamp';
  ts.textContent = message.timestamp || '';
  msgEl.appendChild(ts);
  messagesContainer.appendChild(msgEl);
  messagesContainer.scrollTop = messagesContainer.scrollHeight;
}

chooseFileNormal.addEventListener('change', () => {
  if (chooseFileNormal.checked) {
    chooseFileFancy.checked = false;
  }
});

chooseFileFancy.addEventListener('change', () => {
  if (chooseFileFancy.checked) {
    chooseFileNormal.checked = false;
  }
});

sendButton.addEventListener('click', sendMessage);

clearButton.addEventListener('click', () => {
  vscode.postMessage({ command: 'clearChat' });
});

messageInput.addEventListener('keypress', (e) => {
  if (e.key === 'Enter') {sendMessage();}
});

window.addEventListener('message', (event) => {
  const message = event.data;
  if (message.command === 'receiveMessage') {
    addMessageToUI(message.message);
  } else if (message.command === 'clearChat') {
    messagesContainer.innerHTML = '<div class="welcome-message">Architecture Chat. Cum te pot ajuta astăzi?</div>';
  }
});