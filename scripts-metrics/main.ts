import * as readline from 'readline';
import * as fs from 'fs';
import * as path from 'path';
import { calculateWMC } from './weightedMethodsClass';
import { calculateLCOM } from './lcom';
import { calculateEfferentCoupling } from './efferentCoupling';
import { calculateCyclomaticComplexity } from './cyclomaticComplexity';
import { calculateCognitiveComplexity } from './cognitiveComplexity';
import { calculateAfferentCoupling } from './afferentCoupling';
import { randomBytes } from 'crypto';

const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout
});

interface Metrics {
    wmc: number;
    lcom: number;
    efferentCoupling: number;
    cyclomaticComplex: number;
    cognitiveComplex: number;
    afferentCoupling: number;
}

interface ResultObject {
    metrics: Metrics;
    problem: string;
    solution: string;
}

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
            
            if(isFile && input !== 'stop'){

                const javaFile = path.join(testFileFolderPath, input);
                console.log("path found to file: " + javaFile);
                if(!fs.existsSync(javaFile)){
                    console.log("Acest fisier nu este gasit in folder ul cu test");
                    return resolve("{}");
                }

                console.log("path found to file: " + javaFile);
                let resultWMC: number = calculateWMC(javaFile);
                let resultLcom: number = calculateLCOM(javaFile);
                let resultEfferent: number = calculateEfferentCoupling(javaFile);
                let resultCyclomatic: number = calculateCyclomaticComplexity(javaFile);
                let resultCognitiveComplexity: number = calculateCognitiveComplexity(javaFile);
                let resultAfferentCoupling: number = calculateAfferentCoupling(javaFile);

                const resultObject: ResultObject = {
                    metrics: {
                        wmc: resultWMC,
                        lcom: resultLcom,
                        efferentCoupling: resultEfferent,
                        cyclomaticComplex: resultCyclomatic,
                        cognitiveComplex: resultCognitiveComplexity,
                        afferentCoupling: resultAfferentCoupling
                    },
                    problem: "",
                    solution: ""
                };

                return resolve(JSON.stringify(resultObject));
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
    ///home/paaull/aiprojectvsextension/scripts-metrics/testFiles/GenericHibernateRepo.java
    let currentIndex = readIndex(userPath);
    console.log("Current index for the user " + name + " is " + currentIndex);

    let stop = false;

    while (!stop) {
        const input = await ask("Enter a filename or >stop<: ", testFileDir, true);
        if (input.trim() === "" || input.trim().toLowerCase() === "stop") {
            stop = true;
        } else {
            const resultObject = JSON.parse(input);

            console.log("Result is: ")
            console.log(JSON.stringify(resultObject, null, 2));

            fs.writeFileSync(userPath +'/result_' + name + currentIndex + randomBytes(2) +'.json', JSON.stringify(resultObject, null, 2), 'utf-8');
            currentIndex++;
        }
    }

    updateIndex(userPath, currentIndex);
    rl.close();
}

main();
