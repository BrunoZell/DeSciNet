# Human Movement Model 2: Airport Hops

In this model, we assume that when a person is near an airport, the probability of jumping to another airport is high. This incorporates the idea of long-distance travel via airports.

## Structural Causal Model Specification

### Variables

- **Endogenous Variables (I)**:
  - $H_{\text{longitude}}$: Human longitude
  - $H_{\text{latitude}}$: Human latitude
  - $\epsilon$: Magnitude of change parameter
  - $P_{\text{jump}}$: Probability of jumping to another airport

- **Exogenous Variables (J)**:
  - $M_{\text{longitude}}$: Measured longitude
  - $M_{\text{latitude}}$: Measured latitude
  - $A_{\text{longitude}}$: Airport longitude
  - $A_{\text{latitude}}$: Airport latitude

### Structured Equations

#### Endogenous Variables

1. **H-longitude**:
   $$
   H_{\text{longitude}}(t) = 
   \begin{cases} 
   A_{\text{longitude}}(t_{\text{jump}}) & \text{with probability } P_{\text{jump}} \\
   M_{\text{longitude}}(t_{\text{latest}}) + \epsilon \cdot \sqrt{t - t_{\text{latest}}} & \text{otherwise}
   \end{cases}
   $$
   where:
   - $M_{\text{longitude}}(t_{\text{latest}})$ is the latest measurement of longitude.
   - $\epsilon$ is a parameter controlling the magnitude of the change.
   - $t$ is the current time.
   - $t_{\text{latest}}$ is the time of the latest measurement.
   - $A_{\text{longitude}}(t_{\text{jump}})$ is the longitude of the destination airport at the time of the jump.
   - $P_{\text{jump}}$ is the probability of jumping to another airport.

2. **H-latitude**:
   $$
   H_{\text{latitude}}(t) = 
   \begin{cases} 
   A_{\text{latitude}}(t_{\text{jump}}) & \text{with probability } P_{\text{jump}} \\
   M_{\text{latitude}}(t_{\text{latest}}) + \epsilon \cdot \sqrt{t - t_{\text{latest}}} & \text{otherwise}
   \end{cases}
   $$
   where:
   - $M_{\text{latitude}}(t_{\text{latest}})$ is the latest measurement of latitude.
   - $\epsilon$ is a parameter controlling the magnitude of the change.
   - $t$ is the current time.
   - $t_{\text{latest}}$ is the time of the latest measurement.
   - $A_{\text{latitude}}(t_{\text{jump}})$ is the latitude of the destination airport at the time of the jump.
   - $P_{\text{jump}}$ is the probability of jumping to another airport.

### Proof of Implementation of Partial Model

The partial model specifies:
$$
H_{\text{longitude}}(t) = M_{\text{longitude}}(t)
$$
$$
H_{\text{latitude}}(t) = M_{\text{latitude}}(t)
$$
where $M_{\text{longitude}}(t)$ and $M_{\text{latitude}}(t)$ are only known at sparse measurable points in time.

When $t = t_{\text{latest}}$, the equations in this model reduce to:
$$
H_{\text{longitude}}(t_{\text{latest}}) = 
\begin{cases} 
A_{\text{longitude}}(t_{\text{jump}}) & \text{with probability } P_{\text{jump}} \\
M_{\text{longitude}}(t_{\text{latest}}) + \epsilon \cdot \sqrt{t_{\text{latest}} - t_{\text{latest}}} & \text{otherwise}
\end{cases}
= 
\begin{cases} 
A_{\text{longitude}}(t_{\text{jump}}) & \text{with probability } P_{\text{jump}} \\
M_{\text{longitude}}(t_{\text{latest}}) & \text{otherwise}
\end{cases}
$$
$$
H_{\text{latitude}}(t_{\text{latest}}) = 
\begin{cases} 
A_{\text{latitude}}(t_{\text{jump}}) & \text{with probability } P_{\text{jump}} \\
M_{\text{latitude}}(t_{\text{latest}}) + \epsilon \cdot \sqrt{t_{\text{latest}} - t_{\text{latest}}} & \text{otherwise}
\end{cases}
= 
\begin{cases} 
A_{\text{latitude}}(t_{\text{jump}}) & \text{with probability } P_{\text{jump}} \\
M_{\text{latitude}}(t_{\text{latest}}) & \text{otherwise}
\end{cases}
$$

Since $P_{\text{jump}}$ is the probability of jumping to another airport, and assuming that at the exact time of measurement, the jump does not occur (i.e., $P_{\text{jump}} = 0$ at $t_{\text{latest}}$):
$$
H_{\text{longitude}}(t_{\text{latest}}) = M_{\text{longitude}}(t_{\text{latest}})
$$
$$
H_{\text{latitude}}(t_{\text{latest}}) = M_{\text{latitude}}(t_{\text{latest}})
$$

Thus, at the times when measurements are available, this model reduces to the partial model.
