// JavaScript code generators see "./blocks.ts"

import * as Blockly from "blockly/core";
import {
  javascriptGenerator,
  Order,
} from "blockly/javascript";
import "blockly/javascript";

export default function initGenerator() {
  javascriptGenerator.forBlock["when_event"] = (block, generator) => {
    const whenStatements = generator.statementToCode(block, "THEN");
    return (
      'e.whenEvent("' +
      block.getField("EVENT")?.getValue() +
      '", function(m) {\n' +
      whenStatements +
      "\n});\n"
    );
  };

  javascriptGenerator.forBlock["when_right_clicked"] = (block, generator) => {
    const whenStatements = generator.statementToCode(block, "THEN");
    const entity = block.getField("ENTITY")?.getValue();
    return (
      'e.whenEntityRightClicked("' +
      entity +
      '", function(m) {\n' +
      whenStatements +
      "\n});\n"
    );
  };

  javascriptGenerator.forBlock["when_command"] = (block, generator) => {
    const whenStatements = generator.statementToCode(block, "THEN");
    const command = block.getField("COMMAND")?.getValue();
    return (
      'e.whenCommand("' + command + '", function(m) {\n' + whenStatements + "\n});\n"
    );
  };

  javascriptGenerator.forBlock["when_inside"] = (block, generator) => {
    const whenStatements = generator.statementToCode(block, "THEN");
    const areaName = block.getField("AREA")?.getValue();
    return (
      'e.whenInside("' + areaName + '", function(m) {\n' + whenStatements + "\n});\n"
    );
  };

  javascriptGenerator.forBlock["narrate"] = (block, generator) => {
    const name = block.getField("ENTITY")?.getValue();
    const text = generator.valueToCode(block, "TEXT", Order.ATOMIC);

    return 'm.narrate("' + name + '", ' + text + ");\n";
  };

  javascriptGenerator.forBlock["minecraftCommand"] = (block, generator) => {
    const command = generator.valueToCode(block, "COMMAND", Order.ATOMIC);
    return 'm.cmd(' + command + ');\n';
  };

  javascriptGenerator.forBlock["addRemoveItem"] = (block, generator) => {
    const amount = generator.valueToCode(block, "AMOUNT", Order.ATOMIC);
    const item = generator.valueToCode(block, "ITEM", Order.ATOMIC);
    return "m.addRemoveItem(" + amount + ", " + item + ");\n";
  };

  javascriptGenerator.forBlock["showTitle"] = (block, generator) => {
    const text = generator.valueToCode(block, "TEXT", Order.ATOMIC);

    return "m.title(" + text + ");\n";
  };

  javascriptGenerator.forBlock["items"] = (block) => [
    block.getField("ITEM")?.getValue(),
    Order.ATOMIC,
  ];

  javascriptGenerator.forBlock["itemHeld"] = () => [
    "m.player().getItemInHand(HandTypes.MAIN_HAN)",
    Order.ATOMIC,
  ];

  javascriptGenerator.forBlock["lastPlayerJoined"] = () => [
    "Erik",
    Order.ATOMIC,
  ];
}

export const generate = (workspace: Blockly.WorkspaceSvg) =>
  javascriptGenerator.workspaceToCode(workspace);
