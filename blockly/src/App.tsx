import { useEffect, useState } from "react";
import Editor from "@monaco-editor/react";

import { BlocklyWorkspace } from "./blockly/Workspace";
import { generate } from "./blockly/storeys/code";

// @ts-ignore
import classes from "./app.module.css";

const App = () => {
  const [tab, setTab] = useState(1);
  const [token, setToken] = useState("");
  // @ts-ignore
  const [workspace, setWorkspace] = useState<Blockly.WorkspaceSvg>();
  const [code, setCode] = useState("");

  useEffect(() => {
    async function login() {
      const urlParams = new URL(window.location.href).searchParams;
      const response = await fetch(
        "/login/" + encodeURI(urlParams.get("code") || "")
      );
      setToken(await response.text());
    }
    login();
  }, []);

  return (
    <div className={classes.page}>
      <header className={classes.header}>
        <button onClick={() => setTab(1)}>blockly</button>
        <button onClick={() => setTab(2)}>code</button>
      </header>

      {tab === 1 && (
        <>
          <BlocklyWorkspace
            workspace={workspace}
            onWorkspaceChange={(workspace) => {
              const generatedCode = generate(workspace);
              setCode(generatedCode);
              setWorkspace(workspace);
              let formData = new FormData();
              formData.append("file", new Blob([generatedCode]));
              fetch("/code/upload", {
                method: "POST",
                body: formData,
                headers: {
                  Authorization: `bearer ${token}`,
                },
              });
            }}
          />
          <textarea value={code} className={classes.aside} readOnly />
        </>
      )}
      {tab === 2 && (
        <Editor
          height="90vh"
          defaultLanguage="typescript"
          defaultValue={generate(workspace)}
        />
      )}
    </div>
  );
};

export default App;
