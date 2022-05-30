import * as React from 'react'

declare global {
  namespace JSX {
    interface IntrinsicElements {
      block: any;
      category: any;
      field: any;
      value: any;
      shadow: any;
      xml: any;
    }
  }
}