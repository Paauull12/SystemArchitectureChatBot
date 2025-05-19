export function getHtml(styleUri: string, scriptUri: string): string {
  return `<!DOCTYPE html>
  <html lang="en">
  <head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Architecture Chat</title>
    <link href="${styleUri}" rel="stylesheet">
    <!-- Adăugăm biblioteca marked.js pentru conversia Markdown în HTML -->
    <script src="https://cdnjs.cloudflare.com/ajax/libs/marked/4.3.0/marked.min.js"></script>
  </head>
  <body>
    <div class="chat-container">
      <div class="header">
        <h3>Architecture Chat</h3>
        <button id="clearButton" class="clear-button">Clear History</button>
      </div>
      <div id="messages" class="messages">
        <div class="welcome-message">Architecture Chat. Cum te pot ajuta astăzi?</div>
      </div>
      <div class="checkbox-container">
        <label class="checkbox-wrapper">
          <input type="checkbox" id="chooseFileNormal" class="butonCheck">
          <span class="checkbox-label">Choose File</span>
        </label>
        <label class="checkbox-wrapper">
          <input type="checkbox" id="chooseFileFancy" class="butonCheck">
          <span class="checkbox-label">Choose File Fancy</span>
        </label>
      </div>
      <div class="input-area">
        <input type="text" id="messageInput" placeholder="Type a message...">
        <button id="sendButton" class="send-button">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
            <path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"/>
          </svg>
        </button>
      </div>
    </div>
    <script src="${scriptUri}"></script>
  </body>
  </html>`;
}
