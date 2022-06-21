import { Block, Category, Field, Shadow, Value } from "../Block";

export const ToolBoxLogic = () => (
  <Category name="Logic" categorystyle="logic_category">
    <Block type="controls_if" />
    <Block type="logic_compare" />
    <Block type="logic_operation" />
    <Block type="logic_negate" />
    <Block type="logic_boolean" />
    <Block type="logic_ternary" />
  </Category>
);

export const ToolBoxLoops = () => (
  <Category name="Loops" categorystyle="loop_category">
    <Block type="controls_repeat" />
    <Block type="controls_whileUntil" />
    <Block type="controls_forEach" />
  </Category>
);

export const ToolBoxEvents = () => (
  <Category name="Events" colour="160">
    <Block type="when_event" />
    <Block type="when_right_clicked" />
    <Block type="when_command" />
    <Block type="when_inside" />
  </Category>
);

export const ToolBoxActions = () => (
  <Category name="Actions" colour="45">
    <Block type="narrate">
      <Value name="TEXT">
        <Shadow type="text">
          <Field name="TEXT">abc</Field>
        </Shadow>
      </Value>
    </Block>
    <Block type="minecraftCommand">
      <Value name="COMMAND">
        <Shadow type="text">
          <Field name="TEXT">demo</Field>
        </Shadow>
      </Value>
    </Block>
    <Block type="addRemoveItem">
      <Value name="AMOUNT">
        <Shadow type="math_number">
          <Field name="NUM">-1</Field>
        </Shadow>
      </Value>
      <Value name="ITEM">
        <Shadow type="items" />
      </Value>
    </Block>
    <Block type="showTitle">
      <Value name="TEXT">
        <Shadow type="text">
          <Field name="TEXT">abc</Field>
        </Shadow>
      </Value>
    </Block>
    <Block type="items" />
    <Block type="itemHeld" />
    <Block type="lastPlayerJoined" />
    <Block type="math_number" gap="32">
      <Field name="NUM">123</Field>
    </Block>
  </Category>
);
