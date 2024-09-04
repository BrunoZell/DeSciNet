# Human Movement Model 1: Assumption of Normal Movement

In this simple model, we assume that human movement follows a normal distribution centered around the latest measured value with a standard deviation that increases with the square root of the time difference. This means that short travels are more likely, and far travels in a short amount of time are less likely, but as time increases, larger changes become more probable.

## Structural Causal Model Specification

### Variables

- **Endogenous Variables (I)**:
  - $H_{\text{longitude}}$: Human longitude
  - $H_{\text{latitude}}$: Human latitude
  - $\epsilon$: Magnitude of change parameter

- **Exogenous Variables (J)**:
  - $M_{\text{longitude}}$: Measured longitude
  - $M_{\text{latitude}}$: Measured latitude

### Structured Equations

#### Endogenous Variables

1. **H-longitude**:
   $$
   H_{\text{longitude}}(t) = M_{\text{longitude}}(t_{\text{latest}}) + \epsilon \cdot \sqrt{t - t_{\text{latest}}}
   $$
   where:
   - $M_{\text{longitude}}(t_{\text{latest}})$ is the latest measurement of longitude.
   - $\epsilon$ is a parameter controlling the magnitude of the change.
   - $t$ is the current time.
   - $t_{\text{latest}}$ is the time of the latest measurement.

2. **H-latitude**:
   $$
   H_{\text{latitude}}(t) = M_{\text{latitude}}(t_{\text{latest}}) + \epsilon \cdot \sqrt{t - t_{\text{latest}}}
   $$
   where:
   - $M_{\text{latitude}}(t_{\text{latest}})$ is the latest measurement of latitude.
   - $\epsilon$ is a parameter controlling the magnitude of the change.
   - $t$ is the current time.
   - $t_{\text{latest}}$ is the time of the latest measurement.

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
H_{\text{longitude}}(t_{\text{latest}}) = M_{\text{longitude}}(t_{\text{latest}}) + \epsilon \cdot \sqrt{t_{\text{latest}} - t_{\text{latest}}} = M_{\text{longitude}}(t_{\text{latest}})
$$
$$
H_{\text{latitude}}(t_{\text{latest}}) = M_{\text{latitude}}(t_{\text{latest}}) + \epsilon \cdot \sqrt{t_{\text{latest}} - t_{\text{latest}}} = M_{\text{latitude}}(t_{\text{latest}})
$$

Thus, at the times when measurements are available, this model reduces to the partial model.
