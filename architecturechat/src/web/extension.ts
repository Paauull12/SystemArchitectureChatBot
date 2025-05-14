import * as vscode from 'vscode';
import { getHtml } from './templates/htmlTemplate';

export function activate(context: vscode.ExtensionContext) {
  console.log('Extension "architecturechat" is now active in the web extension host!');

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

  context.subscriptions.push(clearChatCommand, shareCodeCommand);
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
          case 'chooseFileNormal':
            this._handleChooseFileNormal();
            return;  
        }
      }
    );
  }


  private async _handleChooseFileNormal(){
    const fileUris = await vscode.window.showOpenDialog({
      canSelectMany: true,
      openLabel: "Choose .java files",
      filters : {
        "Java Files" : ['java']
      }
    });

    if(fileUris && fileUris.length > 0){
      fileUris.forEach(uri => {
        const filePath = uri.fsPath;
        const fileName = filePath.split(/[/\\]/).pop() || 'unknown';
        vscode.window.showInformationMessage(`Fișierul a fost încărcat cu succes!\nNume: ${fileName}`);
        vscode.window.showInformationMessage(`Calea completă: ${filePath}`);
      });
    }else{
      vscode.window.showErrorMessage("Nu s-au SELECTAT FISIERE!");
    }

  }

  private _handleIncomingMessage(text: string): void {
    if (!text || !this._view) {return;}

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
    if (!this._view) {return;}
    
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
    if (!this._view) {return;}
    
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
    const styleUri = webview.asWebviewUri(
      vscode.Uri.joinPath(this._extensionUri, 'media', 'style.css')
    );
    const scriptUri = webview.asWebviewUri(
      vscode.Uri.joinPath(this._extensionUri, 'media', 'script_js_indexMain.js')
    );
  
    return getHtml(styleUri.toString(), scriptUri.toString());
  }
}

export function deactivate() {}