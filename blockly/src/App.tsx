import { Block, Category, Value, Shadow, Field } from "./blockly/Block";
import { BlocklyComponent } from "./blockly/BlocklyComponent";

import classes from "./app.module.css";

const App = () => (
  <div className={classes.page}>
    <header className={classes.header}></header>
    <BlocklyComponent
      initialXml={`<xml xmlns="http://www.w3.org/1999/xhtml"></xml>`}
    >
      <Category name="Logic" categorystyle="logic_category">
        <Block type="controls_if" />
        <Block type="logic_compare" />
        <Block type="logic_operation" />
        <Block type="logic_negate" />
        <Block type="logic_boolean" />
        <Block type="logic_ternary" />
      </Category>
      <Category name="Loops" categorystyle="loop_category">
        <Block type="controls_repeat" />
        <Block type="controls_whileUntil" />
        <Block type="controls_forEach" />
      </Category>
      <Category name="Events" colour="160">
        <Block type="when_event" />
        <Block type="when_right_clicked" />
        <Block type="when_command" />
        <Block type="when_inside" />
      </Category>
      <Category name="Actions" colour="45">
        <Block type="narrate">
          <Value name="TEXT">
            <Shadow type="text">
              <Field name="TEXT">abc</Field>
            </Shadow>
          </Value>
        </Block>
        <Block type="minecraftCommand" />
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
      <Category name="Text" categorystyle="text_category">
        <Block type="text"></Block>
        <Block type="text_join"></Block>
        <Block type="text_append">
          <Value name="TEXT">
            <Shadow type="text"></Shadow>
          </Value>
        </Block>
        <Block type="text_length">
          <Value name="VALUE">
            <Shadow type="text">
              <Field name="TEXT">abc</Field>
            </Shadow>
          </Value>
        </Block>
        <Block type="text_isEmpty">
          <Value name="VALUE">
            <Shadow type="text">
              <Field name="TEXT"></Field>
            </Shadow>
          </Value>
        </Block>
        <Block type="text_indexOf">
          <Value name="VALUE">
            <Block type="variables_get">
              <Field name="VAR">text</Field>
            </Block>
          </Value>
          <Value name="FIND">
            <Shadow type="text">
              <Field name="TEXT">abc</Field>
            </Shadow>
          </Value>
        </Block>
        <Block type="text_charAt">
          <Value name="VALUE">
            <Block type="variables_get">
              <Field name="VAR">text</Field>
            </Block>
          </Value>
        </Block>
        <Block type="text_getSubstring">
          <Value name="STRING">
            <Block type="variables_get">
              <Field name="VAR">text</Field>
            </Block>
          </Value>
        </Block>
        <Block type="text_changeCase">
          <Value name="TEXT">
            <Shadow type="text">
              <Field name="TEXT">abc</Field>
            </Shadow>
          </Value>
        </Block>
        <Block type="text_trim">
          <Value name="TEXT">
            <Shadow type="text">
              <Field name="TEXT">abc</Field>
            </Shadow>
          </Value>
        </Block>
        <Block type="text_count">
          <Value name="SUB">
            <Shadow type="text"></Shadow>
          </Value>
          <Value name="TEXT">
            <Shadow type="text"></Shadow>
          </Value>
        </Block>
        <Block type="text_replace">
          <Value name="FROM">
            <Shadow type="text"></Shadow>
          </Value>
          <Value name="TO">
            <Shadow type="text"></Shadow>
          </Value>
          <Value name="TEXT">
            <Shadow type="text"></Shadow>
          </Value>
        </Block>
        <Block type="text_reverse">
          <Value name="TEXT">
            <Shadow type="text"></Shadow>
          </Value>
        </Block>
      </Category>
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
  </div>
);

export default App;
