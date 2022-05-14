import { ReactNode, useLayoutEffect, useRef, useState } from "react";

import "blockly/blocks";
import Blockly from "blockly/core";
import locale from "blockly/msg/en";

import initBlocks from "./storeys/blocks";
import initGenerator, { generate } from "./storeys/code";
import classes from "./blockly.module.css";

type BlocklyComponentProps = {
  initialXml?: string;
  children?: ReactNode;
};

export const BlocklyComponent = ({
  initialXml,
  children,
  ...rest
}: BlocklyComponentProps) => {
  const ref = useRef<HTMLDivElement>(null);
  const toolbox = useRef();
  const [workspace, setWorkspace] = useState<any>();

  // window.generateCode = () => {
  //   var code = generate(workspace)
  //   console.log(code);
  // }

  useLayoutEffect(() => {
    Blockly.setLocale(locale);
    initBlocks();
    initGenerator();
    const w = Blockly.inject(ref.current, {
      toolbox: toolbox.current,
      media:
        "https://unpkg.com/blockly@^8.0.2/media/",
      rendererOverrides: {
        ADD_START_HATS: true,
      },
      ...rest,
    });
    if (initialXml) {
      Blockly.Xml.domToWorkspace(Blockly.Xml.textToDom(initialXml), w);
    }
    setWorkspace(w);
  }, []);

  return (
    <>
      <div ref={ref} className={classes.blockly} />
      <xml
        xmlns="https://developers.google.com/blockly/xml"
        is="blockly"
        style={{ display: "none" }}
        ref={toolbox}
      >
        {children}
      </xml>
    </>
  );
};
function generateCode(workspace: undefined) {
  throw new Error("Function not implemented.");
}

