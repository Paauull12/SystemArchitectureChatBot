const vscode = acquireVsCodeApi();
const messagesContainer = document.getElementById('messages');
const messageInput = document.getElementById('messageInput');
const sendButton = document.getElementById('sendButton');

vscode.postMessage({ command: 'initialized' });

function sendMessage() {
  const text = messageInput.value;
  if (text.trim() === '') return;
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
    msgEl.innerHTML = '<pre><code>' + escapeHtml(message.text) + '</code></pre>';
  } else {
    msgEl.innerHTML = escapeHtml(message.text).replace(/\n/g, '<br>');
  }
  const ts = document.createElement('div');
  ts.className = 'time-stamp';
  ts.textContent = message.timestamp || '';
  msgEl.appendChild(ts);
  messagesContainer.appendChild(msgEl);
  messagesContainer.scrollTop = messagesContainer.scrollHeight;
}

sendButton.addEventListener('click', sendMessage);
messageInput.addEventListener('keypress', (e) => {
  if (e.key === 'Enter') sendMessage();
});

window.addEventListener('message', (event) => {
  const message = event.data;
  if (message.command === 'receiveMessage') {
    addMessageToUI(message.message);
  } else if (message.command === 'clearChat') {
    messagesContainer.innerHTML = '<div class="welcome-message">Architecture 1Chat. Cum te pot ajuta astăzi?</div>';
  }
});