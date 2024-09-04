# SCM Framework

Let's use insights from the Structured Causal Model (SCM) literature to define a framework for measurement and modeling around Constellations Metagraphs.

## Standard Structural Causal Model Definition

A Structured Causal Model (SCM) defines relationships between variables where some variables (endogenous) are influenced by others (exogenous and endogenous). The formal definition, as presented in *Causality: Models, Reasoning, and Inference* by Judea Pearl, is as follows:

A causal model is a triple

\[ M = \langle U, V, F \rangle, \]

where:

1. **U** is a set of background variables (exogenous), determined by factors outside the model;
2. **V** is a set \(\{V_1, V_2, \ldots, V_n\}\) of variables, called endogenous, determined by variables in the model—specifically, variables in \(U \cup V\);
3. **F** is a set of functions \(\{f_1, f_2, \ldots, f_n\}\) where each \(f_i\) maps from the domains of \(U_i \cup \text{Pa}_i\) to \(V_i\). Here, \(U_i \subseteq U\) and \(\text{Pa}_i \subseteq V \setminus V_i\), and the entire set \(F\) provides a mapping from \(U \cup V\) to \(V\). The equation

   \[
   v_i = f_i(\text{Pa}_i, u_i), \quad i = 1, \ldots, n,
   \]
   assigns a value to \(V_i\) based on a select set of variables in \(V \cup U\). The set \(F\) ensures a unique solution \(V(u)\).

Every causal model \(M\) can be associated with a directed graph, \(G(M)\), where each node represents a variable, and directed edges point from members of \(\text{Pa}_i\) and \(U_i\) toward \(V_i\). This graph encodes the causal relationships between variables.

Dear Constellation Network enthusiast: note that \(\text{Pa}_i \subseteq V \setminus V_i\) implies that causal models are a DAG (Directed Acyclic Graph).

## Adaptation to Constellation Network Metagraphs

Metagraphs on the Constellation Network define data pipelines, processing data from sources through validation logic and transformations. We can treat these Metagraphs as sources of exogenous variables \(U\), assuming that the data passing through is considered valid or _true_.

We define different types \(T\) of exogenous variables \(u^T\) by specifying a _Metagraph ID_ and a _query function_ that reduces the state of that Metagraph to a value \(u^T_t\), where \(t\) is a timestamp, and \(T\) is a tuple (Metagraph ID, query function).

Each \(t_i\) is a Global Snapshot Ordinal, with \(i\) increasing monotonically. To relate this virtual timestamp back to a physical timestamp, we define \( d_i = t_i - t_{i-1} \) as the time interval between consecutive measurements.

## Time-Based Dynamic Causal Model Structure for Metagraphs

As we adapt the SCM framework to Metagraphs, we introduce the following notation. This deviates slightly from the standard SCM framework, but aligns well with the common notation of [cyclic causal models](https://arxiv.org/pdf/1611.06221).

1. **Exogenous Variables \( X_j \):**
   - **Exogenous Variable Type (\(j\)):** Each \(j\) represents a specific class of exogenous variable (e.g., "temperature", "pressure", "Google Timeline").
   - **Instance of Exogenous Variables (\(X_j\)):** For each type \(j\), \(X_j\) represents the set of measured values for that type.
   - **Measurement Ordinal (\(n\)):** \(n\) is the index representing the sequence of measurements for a specific instance of an exogenous variable.
   - **Timestamp of Ordinal (\(t(n)\)):** \(t(n)\) is the timestamp when the \(n\)th measurement was taken.
   - **Set of Time Indices (\(\mathbb{T}_j\)):** The set \(\mathbb{T}_j\) contains all timestamps \(t(n)\) where measurements of \(X_j\) have been observed.
   - **Exogenous Variable Subset at Time \(t\) (\(X_j(t)\)):** For a given time \(t\), \(X_j(t)\) is the subset of \(X_j\) containing all measurements up to and including time \(t\):

     \[
     X_j(t) = \{ X_j(p_j, t(n)) \mid t(n) \leq t, t(n) \in \mathbb{T}_j \}
     \]
     This subset captures the historical data for the exogenous variable type \(j\) up to time \(t\).

2. **Time Indexing:**
   - **Discrete Time Points:** \( t_1, t_2, \dots, t_n \) are discrete time points corresponding to when exogenous measurements occurred, with \(t_n\) being the latest measurement (the current time).
   - **Time Interval Between Measurements:** \( d_j = t_j - t_{j-1} \) is the time interval between two consecutive measurements.

3. **Endogenous Variables \( Y_i(t) \):**
   - **Endogenous Variable Type (\(i\)):** Each \(i\) represents a type of endogenous variable, influenced by other endogenous and exogenous variables.
   - **Endogenous Variable at Time \(t\) (\(Y_j(t)\)):** \(Y_j(t)\) represents the state of the \(i\)th type of endogenous variable at time \(t\).
   - **Parents of an Endogenous Variable (\(\text{Pa}_j\)):**
     - **Endogenous Parents (\(\text{Pa}_Y(j)\)):** The set \(\text{Pa}_Y(j)\) includes the indices of endogenous variables that directly influence \(Y_j(t)\).
     - **Exogenous Parents (\(\text{Pa}_X(j)\)):** The set \(\text{Pa}_X(j)\) includes the indices of exogenous variables that directly influence \(Y_j(t)\).

4. **Structural Equations for Endogenous Variables:**
   - **Structural Equation for \(Y_j(t)\):** The value of the \(j\)th type of endogenous variable at time \(t\) is determined by a function \(f_j\), which depends on:
     - The current and past values of its endogenous parents \(Y_{\text{Pa}_Y(j)}(t')\) for \( t' \leq t \).
     - The current and past values of its exogenous parents \(X_{\text{Pa}_X(j)}(t(n))\) for \( t(n) \leq t \).
     - An unobserved noise term \(\epsilon_j\), accounting for randomness or unmodeled factors.

     \[
     Y_j(t) = f_j\left(Y_{\text{Pa}_Y(j)}(t'), X_{\text{Pa}_X(j)}(t(n)), \epsilon_j\right)
     \]
     This equation models the evolution of \(Y_j(t)\) based on its dependencies.

5. **Complete Model Definition:**
   - **Set of Endogenous Variables at Time \(t\) (\(\mathbf{Y}(t)\)):** The complete set of endogenous variables at time \(t\) is denoted by:

     \[
     \mathbf{Y}(t) = \{ Y_j(t) \mid j \in \mathcal{J} \}
     \]
     Each \(Y_j(t)\) is determined by its structural equation:

     \[
     \mathbf{Y}(t) = \left\{ f_j\left(Y_{\text{Pa}_{\mathcal{J}}(j)}(t'), X_{\text{Pa}_{\mathcal{I}}(j)}(t(n)), \epsilon_j\right) \mid j \in \mathcal{J} \right\}
     \]

###

### Metagraph Architecture

The Constellation Network's Metagraph architecture supports this dynamic causal model by managing the flow of data through Metagraphs, acting as sources of exogenous variables and facilitating the computation of endogenous variables through defined structural equations.

### Data Update Types

The Metagraph defines the following data update types to support the dynamic causal model:

1. **NewExternalVariable:** Define a new type of exogenous variable \( U^T \) by specifying:
   - `vNow`: A set of L0 node URLs and a data application HTTP query path expected to return a well-known data schema.
   - `vNext`: Metagraph ID and an L0+L1 query function that reduces the computed state of that Metagraph to a value.

2. **AdvanceMeasurementSequence:** Refers to a previously defined Variable \( U^T \) and adds a new value \( u^T_{t_i} \) to the set \( U_{t_i} \) for the current snapshot \( t_i \).

3. **NewModel:** Define a new model \( M \) as a set of endogenous variables \( V \). Each \( V_j \) is defined through a structural equation referring to past values of any \( V_i \in V \) in model \( M \) and any \( u^T_{t_i} \in U_{t_i} \).

### L0 State

### Optimization Goal

Each model \( M \) aims to predict the value of future exogenous measurements \( u^T_{t_{n+1}} \) given all historic exogenous sets \( U_{t_i} \) where \( t_i \leq t_n \).
