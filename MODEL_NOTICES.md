# JARVIS offline model notices

JARVIS bundles the instruction-tuned **Gemma 3 270M** model in GGUF Q4_K_M format.

- Upstream model: https://huggingface.co/google/gemma-3-270m-it
- GGUF conversion: https://huggingface.co/second-state/gemma-3-270m-it-GGUF
- Model file: `gemma-3-270m-it-Q4_K_M.gguf`
- Model file SHA-256: `57bbce8f2095939bde118bafbb65279c6e36ee8bcb09ee258f7f5d6a7b8e7f39`
- Model license: Google Gemma Terms of Use: https://ai.google.dev/gemma/terms

The model is a compact on-device text model, not a replacement for the online JARVIS backend. Its responses may be less capable than the online model. The app uses it as an offline chat fallback only; phone actions remain in the existing local command router and confirmation flows.

The GGUF file is downloaded during APK assembly and SHA-256 verified before packaging. It is not committed to the Git repository.
