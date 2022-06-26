/// <reference types="cypress" />

import '@4tw/cypress-drag-drop'

describe('test blockly by creating command', () => {
  beforeEach(() => {
    cy.visit(Cypress.env('URL'));
  })

  it('create title command', () => {
    cy.get('#blockly-2').click();
    cy.get('.blocklyPath').eq(2).click();
    cy.get('#blockly-3').click();
    cy.get("text").contains('title').click({ force: true });
    cy.get("text").contains('title').move({ deltaX: 0, deltaY: 50 });
    cy.get("text").contains('abc').type("automated test!{enter}");
  });
});
