import * as vscode from 'vscode';
import { getHtml } from './templates/htmlTemplate';
import * as path from 'path';
import { GitLikeMetricSystem } from './gitLikeSystemForMetrics';

let metricSystem: GitLikeMetricSystem | undefined;

export function activate(context: vscode.ExtensionContext) {
  console.log('Extension "architecturechat" is now active!');

  metricSystem = new GitLikeMetricSystem();

  const chatViewProvider = new ChatViewProvider(context.extensionUri, context);
  
  context.subscriptions.push(
    vscode.window.registerWebviewViewProvider('architecturechat.chatView', chatViewProvider)
  );

  const clearChatCommand = vscode.commands.registerCommand('architecturechat.clearChat', () => {
    chatViewProvider.clearChat();
  });

  context.subscriptions.push(clearChatCommand);
}

interface ChatMessage {
  text: string;
  sender: 'user' | 'bot';
  timestamp?: string;
  isCode?: boolean;
}

interface CustomFile {
	fileName: string;
	code: string;
}

interface MsgFromBot {
  message: string;
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
            this._handleChooseFileNormal(message.text);
            return;  
		  case 'clearChat':
			this.clearChat();
			return;
        }
      }
    );
  }

  public clearChat(): void {
	this._messages = [];
	this._saveMessages();

	if(this._view) {
		this._view.webview.postMessage({
			command: 'clearChat'
		});
	}
  }

  private async _handleChooseFileNormal(text: string) {
    const fileUris = await vscode.window.showOpenDialog({
      canSelectMany: true,
      openLabel: "Choose .java files",
      filters: {
        "Java Files": ['java']
      }
    });

    if (fileUris && fileUris.length > 0) {
	    const fileNames: string[] = fileUris.map(uri => uri.fsPath);
	 
	    const fileNamesString = fileNames.join(', ');
      const message = `Selected files: ${fileNamesString}`;

      const customFileArray: CustomFile[] = [];

      for(let file of fileNames){

        const file_code = await this._findFileByPathAndGetContent(file);

        const newCustomFile: CustomFile = {
          fileName: file,
          code: file_code
        };

        customFileArray.push(newCustomFile);
      }
      
      let strToShow = "\nThese are the file context: \n";
      for (const cstFile of customFileArray) {
        strToShow += "File name: " + cstFile.fileName + "\n\n";
        strToShow += "File content:\n" + cstFile.code + "\n\n";
        let fileStuff = metricSystem?.getMetricsForFile(cstFile.fileName);
        if(fileStuff) {
          strToShow += "File metrics:\n" + fileStuff.afferentCoupling + "\n";
        }
      }

      let userInput = "This is the user input: " + text + "\n";

      this._handleIncomingMessage(
          userInput + '\n\n' + strToShow + '\n\n',
          customFileArray
        );
    }
  }

  private async _findFileByPathAndGetContent(path: string): Promise<string>{
    try{

      const uri = vscode.Uri.file(path);

      try{
        await vscode.workspace.fs.stat(uri);
      }catch (error){
        vscode.window.showErrorMessage(`File not found ${path}`);
        return "";
      }

      const fileData = await vscode.workspace.fs.readFile(uri);

      const fileContent = new TextDecoder().decode(fileData);

      return fileContent;
    }catch (error){
      const errorMessage = error instanceof Error ? error.message : String(error);
      vscode.window.showErrorMessage(`Error reading file: ${errorMessage}`);
      return "";
    }
  }

  private async _handleIncomingMessage(text: string, fileContext? : CustomFile[]) {
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

    // In a real implementation, you would process the message here
    // and generate an intelligent response
    //const response = `Răspuns la: ${text}`;

    const response = await fetch('http://localhost:5000/api/chat', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({text: text})
    });

    const data = await response.json() as { message: string };

    const botMessage: ChatMessage = { 
      text: data.message, 
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
      vscode.Uri.joinPath(this._extensionUri, 'media', 'script.js')
    );
  
    return getHtml(styleUri.toString(), scriptUri.toString());
  }
}

export function deactivate() {}