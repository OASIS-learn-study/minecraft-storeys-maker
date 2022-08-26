import { ReactNode, useMemo, useLayoutEffect, useRef } from "react";

import "blockly/blocks";
import Blockly from "blockly/core";

// @ts-ignore
import locale from "blockly/msg/en";

import initBlocks from "./storeys/blocks";
import initGenerator from "./storeys/code";
// @ts-ignore
import classes from "./blockly.module.css";
import { debounce } from "./debounce";

type BlocklyComponentProps = {
  workspace?: Blockly.WorkspaceSvg;
  onWorkspaceChange?: (event: any, workspace?: Blockly.WorkspaceSvg) => void;
  children?: ReactNode;
};

function resize() {
  const blocklyArea = document.getElementById("blocklyArea")!;
  const blocklyDiv = document.getElementById("blocklyDiv")!;
  var onresize = function () {
    // Compute the absolute coordinates and dimensions of blocklyArea.
    let element: any = blocklyArea;
    let x = 0;
    var y = 0;
    do {
      x += element.offsetLeft;
      y += element.offsetTop;
      element = element.offsetParent!;
    } while (element);
    // Position blocklyDiv over blocklyArea.
    blocklyDiv.style.left = x + "px";
    blocklyDiv.style.top = y + "px";
    blocklyDiv.style.width = blocklyArea.offsetWidth + "px";
    blocklyDiv.style.height = blocklyArea.offsetHeight + "px";
  };
  window.addEventListener("resize", onresize, false);
  onresize();
}

const TYPES = [
  Blockly.Events.BLOCK_CREATE,
  Blockly.Events.BLOCK_DELETE,
  Blockly.Events.BLOCK_CHANGE,
  Blockly.Events.BLOCK_MOVE,
] as const;

export const BlocklyComponent = ({
  workspace,
  onWorkspaceChange,
  children,
  ...rest
}: BlocklyComponentProps) => {
  const ref = useRef<HTMLDivElement>(null);
  const toolbox = useRef();

  const changeListener = (event: any) => {
    if (TYPES.includes(event.type) && onWorkspaceChange) {
      onWorkspaceChange(event, Blockly.getMainWorkspace());
    }
  };

  const debouncedChangeHandler = useMemo(
    () => debounce(changeListener, 600),
    []
  );

  useLayoutEffect(() => {
    Blockly.setLocale(locale);
    initBlocks();
    initGenerator();
    const blocklyWorkspace = Blockly.inject(ref.current, {
      toolbox: toolbox.current,
      media: "https://unpkg.com/blockly@^8.0.2/media/",
      rendererOverrides: {
        ADD_START_HATS: true,
      },
      ...rest,
    });
    if (workspace) {
      Blockly.Xml.domToWorkspace(workspace, blocklyWorkspace);
    }

    if (onWorkspaceChange) {
      blocklyWorkspace.addChangeListener(debouncedChangeHandler);
    }
    resize();
    Blockly.svgResize(blocklyWorkspace);
    return () => blocklyWorkspace.removeChangeListener(debouncedChangeHandler);
  }, []);

  return (
    <>
      <div id="blocklyArea" className={classes.blockly}>
        No blockly?
      </div>
      <div ref={ref} id="blocklyDiv" style={{ position: "absolute" }}></div>
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
