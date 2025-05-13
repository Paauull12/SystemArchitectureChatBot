export function getHtml(styleUri: string, scriptUri: string): string {
    return /* html */ `
      <!DOCTYPE html>
      <html lang="en">
      <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link href="${styleUri}" rel="stylesheet">
        <title>ArchitectureChat</title>
      </head>
      <body>
        <div class="chat-container">
          <div class="messages" id="messages">
            <div class="welcome-message">Architecture Chat. Cum te pot ajuta astăzi?</div>
          </div>
          <div class="input-area">
            <input type="text" id="messageInput" placeholder="Scrie un mesaj...">
            <button class="send-button" id="sendButton" title="Trimite">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16">
                <path d="M1.5 1.5L14.5 8L1.5 14.5V9.5L8.5 8L1.5 6.5V1.5Z"/>
              </svg>
            </button>
          </div>
        <div class="checkbox-container">
            <label class="checkbox-wrapper">
                <input id="chooseFileNormal" class="butonCheck" type="checkbox" name="chosefile">
                <span class="checkbox-label">Choose File</span>
            </label>
            <label class="checkbox-wrapper">
                <input id="chooseFileSmart" class="butonCheck" type="checkbox" name="chosefile-smart">
                <span class="checkbox-label">Choose File Smart</span>
            </label>
        </div>
        </div>
        <script src="${scriptUri}"></script>
      </body>
      </html>
    `;
  }