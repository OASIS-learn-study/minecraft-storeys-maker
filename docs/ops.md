# Ops

If you want to run on a different host then localhost, you'll need to set the following environment variables:

    storeys_gui = http://<EXTERNAL-IP>:7070/index.html

Also https://www.learn.study/ops.html (pending #276).

### GCP

A _Series N1: f1-micro (1 vCPU, 614 MB memory)_ is too small and crash loops; but
a _Series N1: g1-small (1 vCPU, 1.7 GB memory)_ seems to suffice for 1 or 2 player; otherwise
a _N1 standard_ or
a _e2-medium (2 vCPU, 4 GB memory)_ or more is recommended.

Remember to set the environment variables as above, add a persistent `/data` volume, and create an appropriate firewall rule.

_TODO Cost: $x VM + $y PD + $7 (?) static IP + $z Ingress+Egress = $TBD._
