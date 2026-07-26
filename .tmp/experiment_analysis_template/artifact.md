# Experiment Analysis template contract

## Reference

- Retained reference: `/Users/frankxu/.codex/plugins/cache/openai-curated-remote/openai-templates/0.1.0/skills/artifact-template-experiment-analysis/assets/reference.docx`
- SHA-256: `d823cd0115186b34c01c6e4b4da3be28b64ee73cac849dbd62d6f4bb6385b0fb`
- Render evidence: `/Users/frankxu/Documents/Data_Log_Mgmt/.tmp/experiment_analysis_template/reference_render2`
- Style evidence: `/Users/frankxu/Documents/Data_Log_Mgmt/.tmp/experiment_analysis_template/template-style-evidence.json`
- Page count: 8. Section count: 1.

## Page system

- US Letter, portrait, 8.5 x 11 inches.
- Margins: 1 inch on all sides.
- One section, new-page start; no different first page and no odd/even variation.
- Header and footer are independent and must be retained. Footer contains the report name at left and a PAGE field at right.

## Typography and visual system

- Georgia is the document typeface. Green is the single accent color used for cover text, headings, footer report name, and table headers.
- Cover: small centered report label, centered green rule, two-line large green title, centered author/date near the lower third.
- Heading 1: green, bold Georgia with generous spacing before; Heading 2 and Heading 3 are used on the cover.
- Body: black Georgia, left aligned, approximately 11 pt with restrained paragraph spacing.
- Tables: light gray header fill, green bold header text, thin gray borders, black body text, bold row-label column.
- Bullets and numbering use real numbering definitions from `word/numbering.xml`; preserve numbering IDs and indents.

## Components and content flow

1. Cover.
2. Document control, purpose, business context, experiment summary.
3. Design notes, hypothesis, success criteria.
4. Pre-launch validation and sample/exposure summary.
5. Primary outcome, guardrails, and segment results.
6. Data quality, limitations, interpretation, and decision log.
7. Post-test actions and metric definitions.
8. Analyst notes.

## Slot map

- `word/document.xml`: editable cover labels, headings, body placeholder paragraphs, bullet/numbered items, and all table-cell placeholder text. Stable locators are paragraph order/style and table order/row/column coordinates recorded by `inspect_template.py`.
- `word/footer1.xml`: editable only for the `goog_rdk_0` content-control text `Report Name`; PAGE field and surrounding layout are preserve-only.
- The report is a pre-pilot analysis plan. Unknown dates, owners, sample sizes, measured rates, confidence intervals, p-values, and final decisions must be expressed as `待确认`, `待试点`, or `不适用`, never invented.
- All existing sections are supported and should be populated in Chinese. Keep the source order and source components.

## Package preservation

- Editable: `word/document.xml`; the report-name text only within `word/footer1.xml`.
- Preserve-only: `[Content_Types].xml`, `_rels/.rels`, `word/_rels/document.xml.rels`, `word/_rels/fontTable.xml.rels`, `word/header1.xml`, `word/numbering.xml`, `word/settings.xml`, `word/styles.xml`, `word/theme/theme1.xml`, `word/fontTable.xml`, both embedded NotoSansSymbols font files, and `word/media/image1.png`.
- Baseline part hashes are stored in the command output and unpacked package at `/Users/frankxu/Documents/Data_Log_Mgmt/.tmp/experiment_analysis_template/package_reference`.

## Fidelity gates

- Reference remains byte-for-byte unchanged and matches its recorded SHA-256.
- Final remains one Letter portrait section with 1-inch margins.
- Cover composition, green rule, heading hierarchy, table styling, header/footer, page number field, numbering definitions, and recurring footer remain source-derived.
- Preserve-only parts and relationships remain byte-for-byte identical.
- Render every final page and inspect for clipping, overlap, broken tables, missing Chinese glyphs, unexpected pagination, and footer drift.
