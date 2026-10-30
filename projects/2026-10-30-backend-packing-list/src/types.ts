export interface Item {
  id: string;
  name: string;
  category: string;
  qty: number;
  packed: boolean;
}

export interface Trip {
  id: string;
  name: string;
  days: number;
  template: string;
  items: Item[];
}

export interface TemplateItem {
  name: string;
  category: string;
  qty?: number;
  perDay?: number;
  max?: number;
}

export interface Template {
  id: string;
  label: string;
  items: TemplateItem[];
}
