import { Block, Category, Field, Shadow, Value } from "../Block";

export const ToolBarText = () => (
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
);
