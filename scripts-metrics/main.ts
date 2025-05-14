import * as readline from 'readline';
import * as fs from 'fs';
import * as path from 'path';
import { calculateWMC } from './weightedMethodsClass';

const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout
});

function readIndex(folderPath: string): number {
    const indexPath = path.join(folderPath, 'index.txt');
    try {
        if (fs.existsSync(indexPath)) {
            const content = fs.readFileSync(indexPath, 'utf-8');
            const index = parseInt(content.trim(), 10);
            return isNaN(index) ? 1 : index;
        } else {
            fs.writeFileSync(indexPath, '1');
            return 1;
        }
    } catch (error) {
        console.error('Error reading index:', error);
        return 1;
    }
}

function updateIndex(folderPath: string, newIndex: number): void {
    const indexPath = path.join(folderPath, 'index.txt');
    try {
        fs.writeFileSync(indexPath, newIndex.toString());
    } catch (error) {
        console.error('Error updating index:', error);
    }
}

const ask = (prompt: string, testFileFolderPath: string, isFile: boolean): Promise<string> => {
    return new Promise((resolve) => {
        rl.question(prompt, (input) => {
            
            //input here should be a valid fileName from the testFileFolder
            if(isFile){

                const javaFile = path.join(testFileFolderPath, prompt);
                if(!fs.existsSync(javaFile)){
                    console.log("Acest fisier nu este gasit in folder ul cu test");
                    return resolve("{}");
                }

                let resultWMC: number = calculateWMC(javaFile);

            }
            return resolve(input);
        });
    });
};

async function main() {
    const name = await ask("What is your name? ", "", false);
    console.log(`Hello ${name}`);

    const resultDir = path.join(__dirname, 'results');
    if (!fs.existsSync(resultDir)) {
        fs.mkdirSync(resultDir, { recursive: true });
        console.log("Created the directory");
    }

    const testFileDir = path.join(__dirname, 'testFiles');
    if (!fs.existsSync(testFileDir)) {
        fs.mkdirSync(testFileDir, { recursive: true });
        console.log("Created the directory");
    }

    const userPath = path.join(resultDir, name);
    if (!fs.existsSync(userPath)) {
        fs.mkdirSync(userPath, { recursive: true });
        console.log("Created directory for user " + name);
    } else {
        console.log("Directory for user " + name + " exists");
    }

    let currentIndex = readIndex(userPath);
    console.log("Current index for the user " + name + " is " + currentIndex);

    let stop = false;

    while (!stop) {
        const input = await ask("Enter a filename or >stop<: ", testFileDir, true);
        if (input.trim() === "" || input.trim().toLowerCase() === "stop") {
            stop = true;
        } else {
            console.log(`You entered: ${input}`);
        }
    }

    rl.close();
}

main();
