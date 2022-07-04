import { useEffect } from "react";
import Editor, { useMonaco } from "@monaco-editor/react";
import Blockly from "blockly/core";

// @ts-ignore
import Minecraft from '../blockly/storeys/storeys.d.ts?raw';
import { generate } from "../blockly/storeys/code";

type EditorProps = {
  workspace: Blockly.Workspace;
};

export const CodeEditor = ({ workspace }: EditorProps) => {
  const monaco = useMonaco();

  useEffect(() => {
    (async () => {
      monaco?.languages.typescript.javascriptDefaults.addExtraLib(
        Minecraft,
        "../blockly/storeys/storeys"
      );
      monaco?.editor.createModel(Minecraft, 'typescript', monaco.Uri.parse("../blockly/storeys/storeys"));
    })();
  }, [monaco]);

  return (
    <Editor
      height="90vh"
      defaultLanguage="javascript"
      defaultValue={generate(workspace)}
    />
  );
};
