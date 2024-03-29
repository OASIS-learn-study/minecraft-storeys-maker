import Blockly from "blockly/core";

import { Category } from "./Block";
import { BlocklyComponent } from "./BlocklyComponent";
import {
  ToolBoxActions,
  ToolBoxEvents,
  ToolBoxLogic,
  ToolBoxLoops,
} from "./storeys/toolbar";
import { ToolBarText } from "./storeys/text";

type BlocklyWorkspaceProps = {
  workspace?: Blockly.WorkspaceSvg;
  onWorkspaceChange?: (event: any, workspace?: Blockly.WorkspaceSvg) => void;
};

export const BlocklyWorkspace = ({
  workspace,
  onWorkspaceChange,
}: BlocklyWorkspaceProps) => (
  <BlocklyComponent
    workspace={workspace}
    onWorkspaceChange={onWorkspaceChange}
  >
    <ToolBoxLogic />
    <ToolBoxLoops />
    <ToolBoxEvents />
    <ToolBoxActions />
    <ToolBarText />
    <Category
      name="Variables"
      custom="VARIABLE"
      categorystyle="variable_category"
    ></Category>
    <Category
      name="Functions"
      custom="PROCEDURE"
      categorystyle="procedure_category"
    ></Category>
  </BlocklyComponent>
);
