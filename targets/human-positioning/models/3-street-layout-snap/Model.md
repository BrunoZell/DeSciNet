# Human Movement Model 3: Street Layout Snapping

In this model, we assume that human movement is constrained by street layouts. Positions are snapped to the nearest street, and movement follows the street network.

## Structural Causal Model Specification

### Variables

- **Endogenous Variables (I)**:
  - $H_{\text{longitude}}$: Human longitude
  - $H_{\text{latitude}}$: Human latitude
  - $\epsilon$: Magnitude of change parameter

- **Exogenous Variables (J)**:
  - $M_{\text{longitude}}$: Measured longitude
  - $M_{\text{latitude}}$: Measured latitude
  - $S_{\text{longitude}}$: Street longitude
  - $S_{\text{latitude}}$: Street latitude

### Structured Equations

#### Endogenous Variables

1. **H-longitude**:
   $$
   H_{\text{longitude}}(t) = \text{snap}(M_{\text{longitude}}(t_{\text{latest}}) + \epsilon \cdot \sqrt{t - t_{\text{latest}}}, S_{\text{longitude}})
   $$
   where:
   - $M_{\text{longitude}}(t_{\text{latest}})$ is the latest measurement of longitude.
   - $\epsilon$ is a parameter controlling the magnitude of the change.
   - $t$ is the current time.
   - $t_{\text{latest}}$ is the time of the latest measurement.
   - $\text{snap}$ is a function that snaps the position to the nearest street longitude $S_{\text{longitude}}$.

2. **H-latitude**:
   $$
   H_{\text{latitude}}(t) = \text{snap}(M_{\text{latitude}}(t_{\text{latest}}) + \epsilon \cdot \sqrt{t - t_{\text{latest}}}, S_{\text{latitude}})
   $$
   where:
   - $M_{\text{latitude}}(t_{\text{latest}})$ is the latest measurement of latitude.
   - $\epsilon$ is a parameter controlling the magnitude of the change.
   - $t$ is the current time.
   - $t_{\text{latest}}$ is the time of the latest measurement.
   - $\text{snap}$ is a function that snaps the position to the nearest street latitude $S_{\text{latitude}}$.
