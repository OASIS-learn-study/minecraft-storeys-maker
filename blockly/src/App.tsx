import { useEffect, useState } from "react";
import Editor from "@monaco-editor/react";

// @ts-ignore
import classes from "./app.module.css";
import { BlocklyWorkspace } from "./blockly/Workspace";
import { generate } from "./blockly/storeys/code";

const App = () => {
  const [tab, setTab] = useState(1);
  const [token, setToken] = useState("");
  // @ts-ignore
  const [workspace, setWorkspace] = useState<Blockly.WorkspaceSvg>();

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
        <BlocklyWorkspace
          workspace={workspace}
          onWorkspaceChange={(workspace) => {
            setWorkspace(workspace);
            const code = generate(workspace);
            let formData = new FormData();
            formData.append("file", new Blob([code]));
            fetch("/code/upload", {
              method: "POST",
              body: formData,
              headers: {
                Authorization: `bearer ${token}`,
              },
            });
          }}
        />
      )}
      {tab === 2 && (
        <Editor
          height="90vh"
          defaultLanguage="javascript"
          defaultValue={generate(workspace)}
        />
      )}
    </div>
  );
};

export default App;
