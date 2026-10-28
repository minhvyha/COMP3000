# Units, flow behaviour, and dam rules

Status: the following rules are implemented in the Java domain model and tested.
The `.river` parser has not yet been connected to this model. The language syntax
proposal is separate so planned features cannot be mistaken for working syntax.

## Units and time

- One timestep is exactly one day: 86400 seconds.
- A flow entry is a daily average rate in litres per second, L/s.
- A rainfall entry is a daily rainfall depth in millimetres, mm.
- Catchment area is in square metres, m2.
- Dam storage and `level` thresholds are volumes in litres, L.
- Release and retention fractions are dimensionless and lie in [0, 1].
- Displayed days start at 1; array indices start at 0. Index 0 is simulation day 1.
  A same-day response uses that day's rainfall. `delay` explicitly shifts it later.

All physical inputs and results must be finite and non-negative. Zero rainfall,
zero area, zero flow, and zero storage are allowed. Negative rates and fractions,
NaN, infinity, overflow, fractional day counts, and zero divisors are errors.
Validation raises `IllegalArgumentException` at this layer. The future interpreter
will attach source locations and map these to language runtime errors.

Rainfall and flow have separate immutable types to prevent treating an array of
millimetres as an array of litres per second. Input arrays are copied; exporting
flow values returns a copy. Results do not mutate their operands.

## Rainfall and catchment conversion

One millimetre of rain over one square metre supplies one litre:

```text
rain_volume_L[t] = rain_mm[t] * catchment_area_m2
runoff_Lps[t] = rain_volume_L[t] / 86400
```

`RainfallSeries.runoff(area)` implements an immediate same-day catchment response.
All rainfall reaches its river that day, satisfying the simplified within-10-days
assumption. There is no evaporation, infiltration loss, or invented runoff loss
coefficient. Subsequent river routing may take longer than 10 days. The future
language will use one shared rainfall series for all catchments, with distinct
non-overlapping catchment areas to avoid counting rainfall twice.

Example: rain `[1, 0, 0]` mm over 864000 m2 produces `[10, 0, 0]` L/s. Doubling
only rain or only area doubles the resulting rates. Direct A1 flow literals
retain their L/s meaning and do not implicitly become rainfall literals.

## Flow arithmetic

A flow is a finite sequence of non-negative rates. An empty sequence is a valid
zero-water flow. Reading after the stored tail returns 0; a negative index is an
error. Stored trailing zero days are retained.

- Flow plus flow adds matching days and zero-pads the shorter sequence.
- Flow minus flow uses the same alignment and rejects any negative daily result.
- Flow times a finite non-negative scalar scales every entry, retaining length.
- Flow divided by a finite positive scalar scales every entry, retaining length.
- Number arithmetic remains the future interpreter's responsibility. Adding a
  number to a flow and multiplying or dividing one flow by another are unsupported
  physical operations in this contract.

For example, `[1, 2] + [3]` is `[4, 2]`. The water volume of a flow is:

```text
volume_L = 86400 * sum(flow_Lps)
```

Summation uses compensation to reduce floating-point accumulation error.

## Flow routing

`delay(flow, d)` requires a non-negative whole day count. For a non-empty sequence
of length L it prepends d zero days and produces length L+d. Zero delay is the
identity. An empty flow remains empty.

```text
delay([8, 4], 1) = [0, 8, 4]
```

`retain(flow, r)` holds fraction r of today's inflow until tomorrow, releasing the
rest today. Previously held water is released in full the next day and is not
retained again. For r>0 and a non-empty sequence the length grows by one. For r=0
the length is unchanged. For r=1 the result is a one-day delay.

```text
y[t] = (1-r) * x[t] + r * x[t-1]
retain([8, 4], 0.25) = [6, 5, 1]
```

`spread(flow, n)` requires a positive whole day count. It distributes every input
entry equally across n days starting on the arrival day. Overlapping contributions
are added. The output length is L+n-1 for a non-empty flow. One-day spread is the
identity; an empty input remains empty.

```text
spread([6, 3], 3) = [2, 3, 3, 1]
```

All three operations preserve total volume within floating-point precision and
keep complete tails. A retention fraction is the fraction held; a dam release
fraction is the fraction released. They intentionally have different meanings.

## Dam rules and update order

Each dam begins empty. Rules and their numeric parameters are immutable, while
storage is local to one simulation run. Reusing a policy does not share storage.

A rule compares one factor with a finite non-negative threshold and supplies a
fixed release fraction. Supported factors are:

- `inflow`: today's incoming L/s.
- `rain`: today's rainfall in mm.
- `level`: storage in litres at the end of the previous day, before today's inflow.

Comparisons are `<`, `<=`, `==`, `>=`, and `>`. Equality is exact double equality;
there is no hidden tolerance in rule selection. Prefer inequalities for measured
or calculated water values. Rules run in written order. The first true comparison
wins, even if later comparisons are also true. A default fraction is mandatory.
An empty rule list with a valid default is a fixed-fraction dam.

Fractions in all rules and in the default are validated when the policy is built.
They must be finite and in [0,1]. Invalid values are rejected, not clamped. This
version supports numeric fractions and thresholds, not arbitrary fraction
expressions or general-purpose functions.

One day's update is:

```text
1. Observe today's inflow, today's rain, and yesterday's ending storage.
2. Select the first matching rule or the default.
3. inflow_L = inflow_Lps * 86400
4. available_L = previous_storage_L + inflow_L
5. released_L = fraction * available_L
6. ending_storage_L = available_L - released_L
7. outflow_Lps = released_L / 86400
```

The fraction's denominator is available water, including stored water. The level
condition still observes the pre-inflow level. Rain affects policy selection;
rain falling directly on a reservoir is not an additional input in this model.

At fraction 0 all incoming water is stored. At fraction 1 all available water is
released. At fraction 0.5 half of the available water is released each day,
including on dry days. Outflow can exceed today's inflow by drawing down storage;
it cannot exceed available water. No dam capacity, spillway, evaporation, initial
storage parameter, or forced emptying is included in this milestone.

For `[8, 0, 0]` L/s inflow and three days:

- Closed: output `[0, 0, 0]` L/s; final storage 691200 L.
- Half: output `[4, 2, 1]` L/s; ending storage `[345600, 172800, 86400]` L.
- Open: output `[8, 0, 0]` L/s; final storage 0 L.

The adaptive demonstration uses these ordered rules:

```text
rain >= 5 mm       -> release 0.25
level >= 300000 L  -> release 0.75
default           -> release 0.50
```

With inflow `[8, 0, 0]` and rain `[6, 0, 0]`, output is `[2, 4.5, 0.75]` L/s,
and final storage is 64800 L. Controlled tests hold inflow/level equal while
varying rain, and hold rain/inflow equal while varying level. A separate test
also demonstrates an inflow condition. Thus multiple factors matter through
policy selection, not only through multiplication by incoming water.

Each trace entry records day, inflow, rainfall, starting storage, available water,
selected rule, fraction, released litres, outflow rate, and ending storage.

## Simulation endpoints and water still in transit

Every dam run needs an explicit positive whole-day horizon H. It returns H output
entries and H trace entries. Missing inflow or rain after the input tail is zero.
Rainfall and inflow inputs longer than H are rejected instead of silently cut.
Additional dry days are simulated by increasing H. A half dam is not required to
become exactly empty; the run stops at H and reports remaining storage.

In a network, all dams use the same H and day origin. Routing can produce water
that will arrive after H. `FlowValue.splitAt(H)` explicitly returns the part
within the window and the complete pending tail. The pending tail's first entry
represents day H+1; keep it for the water balance or a later extended run.
Pass the within-window flow to the downstream dam and retain the pending volume
at that physical connection. The two-dam test exercises this exact operation.

The future network interpreter must preserve pending water and apply each dam
once per run. It must not discard a tail to make it fit H or sum all intermediate
river outputs as though they were independent external inputs. Full graph/name
validation and automatic network orchestration are future integration work.

## Conservation, precision, and limits

For each dam day:

```text
previous_storage_L + inflow_L = released_L + ending_storage_L
```

At a shared network endpoint with initially empty dams:

```text
external_input_L = outlet_output_L + total_dam_storage_L + in_transit_L
```

The tests compare values with absolute tolerance 1e-6 in the tested unit plus
relative tolerance 1e-10. For balance checks the unit is litres. These tolerances
apply to verification, not acceptance of invalid fractions or negative values.
The model uses Java doubles without internal display rounding; ordinary floating
point rounding and extreme underflow remain precision limits.

Daily sequences and resulting lengths are limited to 100000 entries. Dam horizons
are between 1 and 100000 days. A spread may evaluate at most 5000000 individual
contributions, preventing excessive work by the direct equal-share algorithm.
Invalid or over-limit requests fail before allocation or heavy work. These are
implementation limits, not assignment requirements, and must be documented in
the eventual interpreter's run instructions.
