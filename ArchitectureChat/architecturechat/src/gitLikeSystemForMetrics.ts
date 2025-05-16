import * as vscode from 'vscode';
import * as path from 'path';


import { calculateAfferentCoupling } from './metrics/afferentCoupling';
import { calculateCognitiveComplexity } from './metrics/cognitiveComplexity';
import { calculateCyclomaticComplexity } from './metrics/cyclomaticComplexity';
import { calculateEfferentCoupling } from './metrics/efferentCoupling';
import { calculateInstabilityDirect } from './metrics/instability';
import { calculateLCOM } from './metrics/lcom';
import { calculateTightClassCohesion } from './metrics/tcc';
import { calculateWMC } from './metrics/weightedMethodsClass';

interface FileMetrics {
    afferentCoupling: number;
    cognitiveComplexity: number;
    cyclomaticComplexity: number;
    efferentCoupling: number;
    instability: number;
    lcom: number;
    tcc: number;
    wmc: number;
    timestamp: number;
}

export class GitLikeMetricSystem {
    /// The main idea behind this class is to make a efficcent way to 
    ///calculate the metrics for every file

    private metricsCache: Map<string, FileMetrics> = new Map();
    private disposables: vscode.Disposable[] = [];
    private debounceTimers: Map<string, NodeJS.Timeout> = new Map();
    private readonly DEBOUNCE_TIME = 3000;

    constructor () {
        this.initialize();
    }


    public initialize(): void {

        const fileWatchar = vscode.workspace.onDidSaveTextDocument(document => {
            this.handleFileSaved(document);
        });

        this.disposables.push(fileWatchar);

        this.calculateMetricsForWrokspace();
    }


    public dispose(): void {

        this.disposables.forEach(d => d.dispose);
        this.disposables = [];

        this.debounceTimers.forEach(timer => clearTimeout(timer));
        this.debounceTimers.clear();
    }

    private handleFileSaved(document: vscode.TextDocument): void {

        const filePath = document.uri.fsPath;

        if(!filePath.endsWith(".java")){
            return;
        }

        this.debounceCalculation(filePath);
    }

    private debounceCalculation(filePath: string): void {
        const fileName = filePath;

        if(this.debounceTimers.has(fileName)){
            clearTimeout(this.debounceTimers.get(fileName));
        }

        const existingMetrics = this.metricsCache.get(fileName);
        const now = Date.now();

        if(existingMetrics && now - existingMetrics.timestamp < this.DEBOUNCE_TIME){
            return;// no need to claculate if there are some an kinda
            //up to date :)
        }

        const timer = setTimeout(() => {
            this.calculateMetricsForFile(fileName);
            this.debounceTimers.delete(fileName);
        }, 500);

        this.debounceTimers.set(fileName, timer);
    }

    private calculateMetricsForFile(filePath: string): void {

        try {
            const afferentCoupling = calculateAfferentCoupling(filePath);
            const cognitiveComplexity = calculateCognitiveComplexity(filePath);
            const cyclomaticComplexity = calculateCyclomaticComplexity(filePath);
            const efferentCoupling = calculateEfferentCoupling(filePath);
            const instability = calculateInstabilityDirect(filePath);
            const lcom = calculateLCOM(filePath);
            const tcc = calculateTightClassCohesion(filePath);
            const wmc = calculateWMC(filePath);

            this.metricsCache.set(filePath, {
                afferentCoupling,
                cognitiveComplexity,
                cyclomaticComplexity,
                efferentCoupling,
                instability,
                lcom,
                tcc,
                wmc,
                timestamp: Date.now()
            });


        }catch (error){
            console.log("what an error " + error);
        }
    }

    private async calculateMetricsForWrokspace(): Promise<void> {

        const javaFiles = await  vscode.workspace.findFiles('**/*.java',  '**/node_modules/**');

        javaFiles.forEach(uri =>{
            this.debounceCalculation(uri.fsPath);
        })
    }

    public getMetricsForFile(fileName: string): FileMetrics | undefined {
        return this.metricsCache.get(fileName);
    }


    public getAllMetrics(): Map<string, FileMetrics> {
        return new Map(this.metricsCache);
    }

    public forceRecalculateMetrics(filePath: string): void {
        this.calculateMetricsForFile(filePath);
    }

}
















