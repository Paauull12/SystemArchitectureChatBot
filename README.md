# ChatBot for Architecture

## What Problem Will Be Solved

We're addressing the challenge of ensuring that your architecture is up to date and that engineers avoid mistakes when developing an application. The ChatBot will respond based either on the context provided (such as code files shared by the user) or on files with detected issues, intelligently retrieved by our system based on metrics.

## Why AI for Architecture?

### Key Benefits:
- **Accelerated Design Process:** It's easier to design when you have an on-demand assistant trained in architectural best practices.
- **Smart Recommendations:** AI-driven suggestions based on best practices, regulations, and current project files.
- **24/7 Availability:** Instant architectural consultation at any time—especially helpful for junior developers who often need more guidance.

## Technology Stack

- **AI/ML:** OpenAI Model API  
- **Backend:** Python with Flask and LangChain  
- **Frontend:** Visual Studio Code Extension  
- **Database:** PostgreSQL – to be implemented  

## Data Overview

### Data Type

The dataset consists of `.java` source files containing classes specifically designed to exemplify various poor coding practices and software design anti-patterns.

---

### Dataset Volume

The current collection comprises approximately 60 distinct class examples, each crafted to illustrate specific instances of suboptimal or erroneous coding techniques.

---

### Data Distribution

The dataset includes erroneous or problematic code examples, categorized as follows within the `georgi-data` subset:

| Category                                                               | Number of Examples | Percentage  |
| ---------------------------------------------------------------------- | ------------------ | ----------- |
| Architecture and General Design Issues                                 | 5                  | 17.24%      |
| Violations of SOLID Principles                                         | 7                  | 24.14%      |
| Testability and Coupling Deficiencies                                  | 5                  | 17.24%      |
| Misapplication of Design Patterns and Object-Oriented Principles       | 3                  | 10.34%      |
| Code Style and Quality Issues                                          | 8                  | 27.59%      |
| Other Code Issues (Distinct Low-Level Bugs Not Categorized Above)      | 1                  | 3.45%       |

## Performance Metrics Monitored

### Cyclomatic Complexity  
Measures the number of independent paths through code; higher values indicate more complex logic.

### Cognitive Complexity  
Quantifies how difficult code is to understand by humans, beyond just control flow structures.

### Weighted Methods per Class (WMC)  
Sums the complexity of all class methods; indicates effort to maintain or understand the class.

### Lack of Cohesion in Methods (LCOM)  
Measures how related class methods are via shared fields; high LCOM implies poor class design.

### Afferent Coupling (Ca)  
Counts how many other classes depend on a given class; high Ca means high responsibility.

### Efferent Coupling (Ce)  
Counts how many classes a given class depends on; high Ce implies high external reliance.

### Tight Class Cohesion (TCC)  
Ratio of directly connected method pairs via shared attributes; high TCC means strong internal cohesion.

### Instability (I)  
Defined as Ce / (Ca + Ce); indicates how prone a module is to change under external influence.

## Current Status

Development – early stages

## Roadmap

- **Phase 1:** Basic conversational AI for architectural queries  
- **Phase 2:** Integration with metrics for every file scanned upon modification  
- **Phase 3:** Retrieval from a vector database for suggestions based on metrics  
- **Phase 4:** Implementation of smart and manual document retrieval to provide additional context to the ChatBot  

## Our Team

Meet the team behind **ChatBot for Architecture**:

---

### 👤 Dobrescu Andrei-Paul
**Role:** Team Lead

**Bio:** Student at BBU enjoy working with a lot of different systems. Integrating Ai in as many places as I can :)).

**Links:**  
- [🔗 LinkedIn](https://linkedin.com/in/username1)  
- [💻 GitHub](https://github.com/username1)

---

### 👤 Cărbune Ecaterina 
**Role:** Data analyst  

**Bio:** Student at Babeș-Bolyai University

**Links:**  
- [🔗 LinkedIn](https://www.linkedin.com/in/ecaterina-carbune-67061b339)  
- [💻 GitHub](https://github.com/Ec4ter1)

---

### 👤 Asandei Georgiana  
**Role:** AI Data Engineer (Code Analysis)  

**Bio:** I am a curious and passionate programmer who enjoys sunny days and bike rides, currently studying CS at BBU.

**Links:**  
- [🔗 LinkedIn](https://linkedin.com/in/georgiana-asandei-079597293)  
- [💻 GitHub](https://github.com/geoqiq)

---

### 👤 Drăghiță Claudiu - Ionuț
**Role:**  TypeScript Developer (AI Tooling)

**Bio:** Student passionate about AI and developing useful applications.

**Links:**  
- [🔗 LinkedIn](https://www.linkedin.com/in/claudiu-dr%C4%83ghi%C8%9B%C4%83-a39199295/) 
- [💻 GitHub](https://github.com/claudiu28)

## Contact

For inquiries or collaborations, reach out to us at: **dobrescuandreipaul@yahoo.com**

---

**Transforming Architecture with AI**  
© 2025 ChatBot for Architecture
