# SCM Framework

## Introduction

Let's brrow from the Structured Causal Model (SCM) literature to define a canvas of measurement and modelling around Constellations Metagraphs.

### Structural Causal Model Definition

Definition 7.1.1 (Structural Causal Model) from _Causality: Models, Reasoning, and Inference_ by Judea Pearl:

A causal model is a triple

\[ M = \langle U, V, F \rangle, \]

where:

1. **U** is a set of background variables (also called exogenous), that are determined by factors outside the model;
2. **V** is a set \(\{V_1, V_2, \ldots, V_n\}\) of variables, called endogenous, that are determined by variables in the model - that is, variables in \(U \cup V\); and
3. **F** is a set of functions \(\{f_1, f_2, \ldots, f_n\}\) such that each \(f_i\) is a mapping from (the respective domains of) \(U_i \cup PA_i\) to \(V_i\), where \(U_i \subseteq U\) and \(PA_i \subseteq V \setminus V_i\), and the entire set \(F\) forms a mapping from \(U \cup V\) to \(V\). In other words, each \(f_i\) in

    \[
    v_i = f_i(pa_i, u_i), \quad i = 1, \ldots, n,
    \]
    assigns a value to \(V_i\) that depends on (the values of) a select set of variables in \(V \cup U\), and the entire set \(F\) has a unique solution \(V(u)\).

Every causal model \(M\) can be associated with a directed graph, \(G(M)\), in which each node corresponds to a variable and the directed edges point from members of \(PA_i\) and \(U_i\) toward \(V_i\). We call such a graph the causal diagram associated with \(M\). This graph encodes the relationship between endogenous and exogenous variables that have direct influence on each other.

### Adaptation to Constellation Network Metagraphs

Metagraphs on Constellation Network define data pipelines from the data source through a filter of validation logic and transformations. Lets use Metagraphs as a data source of exogenous variables \(U\) and consider all data that passes through the Metagraph as _true_.

For that, the network allows to define different types T of Exogenous variables \(u^T\) by specifying a _Metagraph ID_ and a _query function_ that reduces the state of that Metagraph down to a value \(u^T_t\), where \(t\) is a timestamp of the measurement and \(T\) is a tuple (Metagraph ID, query function).

In that definition, every t_i is a Global Snapshot Ordinal with i increasing monotonically as an integer. To relate this virtual timestamp back to a physical timestamp, we define \( d_i = t_i - t_{i-1} \) is the time interval between two consecutive measurements.

### Setup and Notation

1. **Exogenous Variables \( U \)**:

   - For each timestamp \( t_i \), there is a corresponding set \( U_{t_i} = \{u^T_{t_i}\} \) where \( T \) indexes different types of measurements (e.g., \( T \) could be a type like "temperature", "pressure", etc.).
   - \( u^T_{t_i} \) is the value of the exogenous variable of type \( T \) at time \( t_i \).
   - The set \( U_{t_i} \) includes all such measurements at time \( t_i \).
   - For any given \( U_{t_i} \), there can only be one or none measurement \( u^T_{t_i} \) for each type \( T \).

2. **Time Indexing**:

   - \( t_1, t_2, \dots, t_n \) are discrete time points where exogenous measurements occured, with \(t_n\) being the latest measurement _now_.
   - \( d_i = t_i - t_{i-1} \) is the time interval between two consecutive measurements.

3. **Endogenous Variables \( V(t) \)**:

   - \( V(t) \) represents an endogenous variable at any time \( t \), including times between the measurements \( t_i \).
   - \( V(t) \) is influenced by the past exogenous variables and potentially other endogenous variables, and it evolves according to the structural equations.

### Metagraph Architecture

The DeSciNet Metagraph defines following data update types:

- **NewVariables**: Define a new type of exogenous variable \( U^T \) by specifying:
  - vNow: A set of L0 node URLs and a data applicaton HTTP query path that is expected to return a well known data schema.
  - vNext: Metagraph ID and an L0+L1 query function that reduces the computed state of that Metagraph down to a value.
- **NewMeasurement**: Refers to a previously defined Variable \( U^T \) and adds a new value \( u^T_{t_i} \) to the set \( U_{t_i} \) of the current snapshot \( t_i \).
- **NewModel**: Define a new model \( M \) that is defined as a set of endogenous variables \( V \) with each \( V_i \) being defined through a structural equation that refers to past values of any \( V_i \in V \) in the model \( M \) and any \( u^T_{t_i} \in U_{t_i} \).

### Optimization Goal

Each model \( M \) aims to predict the value of future exogenous measurements \( u^T_{t_{n+1}} \) given all historic exogenous sets \( U_{t_i} \) with \( t_i \leq t_n \).
