# Human Movement Model 1: Assumption of Normal Movement

In this simple model, we assume that human movement follows a normal distribution centered around the latest measured value with a standard deviation that increases with the square root of the time difference. This means that short travels are more likely, and far travels in a short amount of time are less likely, but as time increases, larger changes become more probable.

## Structural Causal Model (SCM)

### Variables

- **Endogenous Variables (I)**:
  - $H_{\text{longitude}}$: Human longitude
  - $H_{\text{latitude}}$: Human latitude
  - $\epsilon$: Magnitude of change parameter

- **Exogenous Variables (J)**:
  - $M_{\text{longitude}}$: Measured longitude
  - $M_{\text{latitude}}$: Measured latitude

### Mathematical Equations

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

#### Probabilistic Form

To introduce the probabilistic nature, we assume that the actual value follows a normal distribution centered around the latest measured value with a standard deviation that increases with the square root of the time difference:

1. **Probabilistic H-longitude**:
   $$
   H_{\text{longitude}}(t) \sim \mathcal{N}\left(M_{\text{longitude}}(t_{\text{latest}}), \sigma_{\text{longitude}}^2\right)
   $$
   where:
   - $\sigma_{\text{longitude}} = \epsilon \cdot \sqrt{t - t_{\text{latest}}}$ is the standard deviation.

2. **Probabilistic H-latitude**:
   $$
   H_{\text{latitude}}(t) \sim \mathcal{N}\left(M_{\text{latitude}}(t_{\text{latest}}), \sigma_{\text{latitude}}^2\right)
   $$
   where:
   - $\sigma_{\text{latitude}} = \epsilon \cdot \sqrt{t - t_{\text{latest}}}$ is the standard deviation.

### Inserting Measurements

Measurements are inserted into the exogenous variables $M$ using the `addMeasurement` function. For example, when a new observation is received, it updates the latest measurements for `M-longitude` and `M-latitude`.
