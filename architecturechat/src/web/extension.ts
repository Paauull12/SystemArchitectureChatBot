import * as vscode from 'vscode';

export function activate(context: vscode.ExtensionContext) {
  console.log('Extension "architecturechat" is now active in the web extension host!');

  const helloWorldCommand = vscode.commands.registerCommand('architecturechat.helloWorld', () => {
    vscode.window.showInformationMessage('Hello World from ArchitectureChat in a web extension host!');
  });

  const clearChatCommand = vscode.commands.registerCommand('architecturechat.clearChat', () => {
    if (chatViewProvider) {
      chatViewProvider.clearChat();
    }
  });

  const shareCodeCommand = vscode.commands.registerCommand('architecturechat.shareCode', () => {
    const editor = vscode.window.activeTextEditor;
    if (editor) {
      const selection = editor.selection;
      const text = editor.document.getText(selection);
      
      if (text && chatViewProvider) {
        chatViewProvider.shareCode(text);
      } else {
        vscode.window.showInformationMessage('Selectează cod pentru a-l partaja în chat.');
      }
    }
  });

  const chatViewProvider = new ChatViewProvider(context.extensionUri, context);
  
  context.subscriptions.push(
    vscode.window.registerWebviewViewProvider('architecturechat.chatView', chatViewProvider)
  );

  context.subscriptions.push(helloWorldCommand, clearChatCommand, shareCodeCommand);
}


interface ChatMessage {
  text: string;
  sender: 'user' | 'bot';
  timestamp?: string;
  isCode?: boolean;
}


class ChatViewProvider implements vscode.WebviewViewProvider {
  private _view?: vscode.WebviewView;
  private _messages: ChatMessage[] = [];
  private _context: vscode.ExtensionContext;

  constructor(private readonly _extensionUri: vscode.Uri, context: vscode.ExtensionContext) {
    this._context = context;
    this._loadMessages();
  }

  public resolveWebviewView(
    webviewView: vscode.WebviewView,
    context: vscode.WebviewViewResolveContext,
    _token: vscode.CancellationToken
  ): void {
    this._view = webviewView;

    webviewView.webview.options = {
      enableScripts: true,
      localResourceRoots: [this._extensionUri]
    };

    webviewView.webview.html = this._getHtmlForWebview(webviewView.webview);

    webviewView.webview.onDidReceiveMessage(
      (message: any) => {
        switch (message.command) {
          case 'sendMessage':
            this._handleIncomingMessage(message.text);
            return;
          case 'initialized':
            this._restoreMessages();
            return;
        }
      }
    );
  }


  private _handleIncomingMessage(text: string): void {
    if (!text || !this._view) return;

    const userMessage: ChatMessage = { 
      text, 
      sender: 'user', 
      timestamp: this._getCurrentTime() 
    };
    
    this._messages.push(userMessage);

    this._view.webview.postMessage({
      command: 'receiveMessage',
      message: userMessage
    });

    const response = `Răspuns la: ${text}`;
    const botMessage: ChatMessage = { 
      text: response, 
      sender: 'bot', 
      timestamp: this._getCurrentTime() 
    };
    
    this._messages.push(botMessage);
    this._saveMessages();

    this._view.webview.postMessage({ 
      command: 'receiveMessage', 
      message: botMessage
    });
  }

  private _restoreMessages(): void {
    if (!this._view) return;
    
    this._messages.forEach(msg => {
      this._view!.webview.postMessage({
        command: 'receiveMessage',
        message: msg
      });
    });
  }


  public clearChat(): void {
    this._messages = [];
    this._saveMessages();
    
    if (this._view) {
      this._view.webview.postMessage({ command: 'clearChat' });
    }
  }


  public shareCode(code: string): void {
    if (!this._view) return;
    
    const formattedCode = '```\n' + code + '\n```';
    const codeMessage: ChatMessage = { 
      text: formattedCode, 
      sender: 'user', 
      timestamp: this._getCurrentTime(),
      isCode: true 
    };
    
    this._messages.push(codeMessage);
    
    this._view.webview.postMessage({ 
      command: 'receiveMessage', 
      message: codeMessage
    });
    
    const response = "Am primit codul. Cum te pot ajuta cu el?";
    const botMessage: ChatMessage = { 
      text: response, 
      sender: 'bot', 
      timestamp: this._getCurrentTime() 
    };
    
    this._messages.push(botMessage);
    this._saveMessages();
    
    this._view.webview.postMessage({ 
      command: 'receiveMessage', 
      message: botMessage
    });
  }

  private _saveMessages(): void {
    this._context.globalState.update('chatMessages', this._messages);
  }


  private _loadMessages(): void {
    this._messages = this._context.globalState.get('chatMessages', []);
  }

  private _getCurrentTime(): string {
    const now = new Date();
    return now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  }

 
  private _getHtmlForWebview(webview: vscode.Webview): string {
    return `
      <!DOCTYPE html>
      <html lang="en">
      <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <style>
          body { 
            padding: 0; 
            margin: 0;
            font-family: var(--vscode-font-family);
            color: var(--vscode-foreground);
            height: 100vh;
            overflow: hidden;
            display: flex;
            flex-direction: column;
          }
          .chat-container {
            display: flex;
            flex-direction: column;
            height: 100vh;
            position: relative;
          }
          .toolbar {
            display: flex;
            justify-content: flex-end;
            padding: 4px 8px;
            border-bottom: 1px solid var(--vscode-input-border);
            background-color: var(--vscode-editor-background);
          }
          .messages {
            flex: 1;
            overflow-y: auto;
            padding: 8px 12px;
            display: flex;
            flex-direction: column;
          }
          .message {
            margin: 8px 0;
            padding: 10px 12px;
            border-radius: 8px;
            max-width: 85%;
            word-wrap: break-word;
            position: relative;
            box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
          }
          .user {
            background-color: var(--vscode-input-background);
            color: var(--vscode-input-foreground);
            align-self: flex-end;
            margin-left: auto;
          }
          .bot {
            background-color: var(--vscode-editor-background);
            border: 1px solid var(--vscode-input-border);
            align-self: flex-start;
            margin-right: auto;
          }
          .input-area {
            display: flex;
            padding: 10px 12px;
            background-color: var(--vscode-editor-background);
            border-top: 1px solid var(--vscode-input-border);
            position: sticky;
            bottom: 0;
            width: 100%;
            box-sizing: border-box;
          }
          input {
            flex: 1;
            padding: 10px 12px;
            border: 1px solid var(--vscode-input-border);
            background-color: var(--vscode-input-background);
            color: var(--vscode-input-foreground);
            border-radius: 20px;
            font-size: 14px;
          }
          .send-button {
            display: flex;
            align-items: center;
            justify-content: center;
            width: 36px;
            height: 36px;
            margin-left: 8px;
            background-color: var(--vscode-button-background);
            color: var(--vscode-button-foreground);
            border: none;
            border-radius: 50%;
            cursor: pointer;
            transition: background-color 0.2s;
          }
          .send-button:hover {
            background-color: var(--vscode-button-hoverBackground);
          }
          .send-button svg {
            width: 16px;
            height: 16px;
            fill: currentColor;
          }
          .time-stamp {
            font-size: 0.7em;
            color: var(--vscode-descriptionForeground);
            margin-top: 4px;
            text-align: right;
          }
          .welcome-message {
            text-align: center;
            margin: 20px 0;
            font-style: italic;
            color: var(--vscode-descriptionForeground);
          }
          pre {
            background-color: var(--vscode-textBlockQuote-background);
            padding: 10px;
            border-radius: 6px;
            overflow-x: auto;
            margin: 8px 0;
          }
          code {
            font-family: var(--vscode-editor-font-family);
            font-size: var(--vscode-editor-font-size);
          }
          .toolbar-btn {
            background: none;
            border: none;
            color: var(--vscode-descriptionForeground);
            cursor: pointer;
            padding: 2px 5px;
            font-size: 0.8em;
          }
          .toolbar-btn:hover {
            color: var(--vscode-foreground);
            background-color: var(--vscode-button-secondaryHoverBackground);
          }
        </style>
      </head>
      <body>
        <div class="chat-container">
          <div class="toolbar">
            <button class="toolbar-btn" id="clearBtn" title="Curăță conversația">
              <svg width="16" height="16" viewBox="0 0 16 16" xmlns="http://www.w3.org/2000/svg" fill="currentColor">
                <path d="M10 3h3v1h-1v9l-1 1H4l-1-1V4H2V3h3V2a1 1 0 0 1 1-1h3a1 1 0 0 1 1 1v1zM9 2H6v1h3V2zM4 13h7V4H4v9zm2-8H5v7h1V5zm1 0h1v7H7V5zm2 0h1v7H9V5z"/>
              </svg>
            </button>
          </div>
          <div class="messages" id="messages">
            <div class="welcome-message">Bine ai venit la Architecture Chat. Cum te pot ajuta astăzi?</div>
          </div>
          <div class="input-area">
            <input type="text" id="messageInput" placeholder="Scrie un mesaj...">
            <button class="send-button" id="sendButton" title="Trimite">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16">
                <path d="M1.5 1.5L14.5 8L1.5 14.5V9.5L8.5 8L1.5 6.5V1.5Z"/>
              </svg>
            </button>
          </div>
        </div>

        <script>
          const vscode = acquireVsCodeApi();
          const messagesContainer = document.getElementById('messages');
          const messageInput = document.getElementById('messageInput');
          const sendButton = document.getElementById('sendButton');
          const clearBtn = document.getElementById('clearBtn');

          // Anunțăm extensia că webview-ul a fost inițializat
          vscode.postMessage({ command: 'initialized' });

          // Funcție pentru trimiterea mesajelor
          function sendMessage() {
            const text = messageInput.value;
            if (text.trim() === '') return;
            
            // Trimite mesajul către extensie
            vscode.postMessage({
              command: 'sendMessage',
              text: text
            });
            
            // Curăță input-ul
            messageInput.value = '';
          }

          // Formatarea codului în mesaj
          function formatMessageText(text, isCode) {
            if (isCode) {
              // Extrage codul din marcajul markdown
              const codeContent = text.replace(/^\`\`\`\\n/, '').replace(/\\n\`\`\`$/, '');
              return '<pre><code>' + escapeHtml(codeContent) + '</code></pre>';
            } else {
              // Pentru mesaje normale, convertim noi linii în <br> și putem adăuga alte formatări
              return escapeHtml(text).replace(/\\n/g, '<br>');
            }
          }

          // Escape HTML pentru a preveni XSS
          function escapeHtml(text) {
            return text
              .replace(/&/g, "&amp;")
              .replace(/</g, "&lt;")
              .replace(/>/g, "&gt;")
              .replace(/"/g, "&quot;")
              .replace(/'/g, "&#039;");
          }

          // Adaugă mesaj în interfață
          function addMessageToUI(message) {
            const messageElement = document.createElement('div');
            messageElement.className = 'message ' + message.sender;
            
            // Verificăm dacă este cod și aplicăm formatarea adecvată
            if (message.isCode) {
              messageElement.innerHTML = formatMessageText(message.text, true);
            } else {
              messageElement.innerHTML = formatMessageText(message.text, false);
            }
            
            // Adăugăm timestamp
            const timeStamp = document.createElement('div');
            timeStamp.className = 'time-stamp';
            timeStamp.textContent = message.timestamp || getCurrentTime();
            messageElement.appendChild(timeStamp);
            
            messagesContainer.appendChild(messageElement);
            scrollToBottom();
          }

          // Obține ora curentă formatată
          function getCurrentTime() {
            const now = new Date();
            return now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
          }

          // Scroll la ultimul mesaj
          function scrollToBottom() {
            messagesContainer.scrollTop = messagesContainer.scrollHeight;
          }

          // Curăță toate mesajele
          function clearMessages() {
            messagesContainer.innerHTML = '<div class="welcome-message">Conversația a fost curățată. Cum te pot ajuta?</div>';
          }

          // Ascultă pentru evenimente
          sendButton.addEventListener('click', sendMessage);
          messageInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') sendMessage();
          });
          
          clearBtn.addEventListener('click', () => {
            vscode.postMessage({ command: 'clearChat' });
          });

          // Focusul automat pe input la deschidere
          messageInput.focus();

          // Procesează mesajele primite de la extensie
          window.addEventListener('message', event => {
            const messageData = event.data;
            switch (messageData.command) {
              case 'receiveMessage':
                addMessageToUI(messageData.message);
                break;
              case 'clearChat':
                clearMessages();
                break;
            }
          });
        </script>
      </body>
      </html>
    `;
  }
}

export function deactivate() {}