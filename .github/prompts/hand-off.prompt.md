---
mode: java-summarizer
---
Generate a concise, professional hand-off summary for this session.

Sacrifice grammar for concision. Cover:
- what was built or modified (file paths included)
- how it works and any relevant API contracts or integrations
- where scripts or code should be executed
- what steps remain

Write as if handing to a new developer with zero prior context — clear, factual, self-contained.

Then append the summary to `docs/workflow-tracker.md` (relative to the current lab root) using **exactly** this format — do not overwrite existing content:

```
### Hand-Off Summary – YYYY-MM-DD

**Stage completed:** [stage name]
**Status:** [what passed / what is pending]
**Files changed:** [list]
**Commands run:** [list]
**Blockers / open items:** [list or "None"]

---
[full summary text]

---
```

If `docs/workflow-tracker.md` does not exist, create it with the above block as the first entry.
