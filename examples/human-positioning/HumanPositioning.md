# Human Movement Model 1: Assumption of Normal Movement

## Partial Structural Causal Model Specification

### Variables

- **Endogenous Variables (I)**:
  - $H_{\text{longitude}}$: Human longitude
  - $H_{\text{latitude}}$: Human latitude

- **Exogenous Variables (J)**:
  - $M_{\text{longitude}}$: Measured longitude
  - $M_{\text{latitude}}$: Measured latitude

### Structured Equations

#### Endogenous Variables

1. **H-longitude**:
   $$
   H_{\text{longitude}}(t) = M_{\text{longitude}}(t)
   $$
   where:
   - $M_{\text{longitude}}(t)$ is the longitude measurement from Google Timeline at time $t$.
   - $M_{\text{longitude}}(t)$ is only known at sparse measurable points in time.

2. **H-latitude**:
   $$
   H_{\text{latitude}}(t) = M_{\text{latitude}}(t)
   $$
   where:
   - $M_{\text{latitude}}(t)$ is the latitude measurement from Google Timeline at time $t$.
   - $M_{\text{latitude}}(t)$ is only known at sparse measurable points in time.
