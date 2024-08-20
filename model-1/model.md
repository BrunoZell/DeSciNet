### Structured Equation Form for HumanMovementModel1

#### **1. Variables**

- $P_i(t) = (L_i(t), \Phi_i(t))$: Position of human $i$ at time $t$, where:
  - $L_i(t)$ is the latitude at time $t$
  - $\Phi_i(t)$ is the longitude at time $t$

#### **2. Structural Equations**

- **Position Update via Walking:**

  $$
  P_i(t + \Delta t) = P_i(t) + (\Delta L_i \times S_i, \Delta \Phi_i \times S_i)
  $$

  Where:
  - $\Delta L_i$ and $\Delta \Phi_i$ are the changes in latitude and longitude, respectively, based on the direction.
  - $S_i$ is the step length for human $i$.

#### **3. Observations**

- **Integrating an Observation:**

  $$
  O_i(t) = P_i(t) = (L_i(t), \Phi_i(t))
  $$

  The observation updates the corresponding position $P_i(t)$ in the model.

#### **4. Interventions**

- **Walk Intervention:**

  $$
  I_i^{\text{Walk}}(t) \rightarrow P_i(t + \Delta t) = P_i(t) + (\Delta L_i \times S_i, \Delta \Phi_i \times S_i)
  $$

  The walk intervention modifies the position $P_i(t)$ based on the direction $(\Delta L_i, \Delta \Phi_i)$ and step length $S_i$.

#### **5. Strategy**

- **Simple Walk Strategy:**

  The strategy generates a random direction and applies the walk intervention:
  
  $$
  \text{Strategy}_i(t) \rightarrow I_i^{\text{Walk}}(t)
  $$

  Where:
  - $I_i^{\text{Walk}}(t)$ is an intervention generated based on the current world state at time $t$.
