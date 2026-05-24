export interface User_Address {
  city: string;
  street: string;
  zipCode?: string;
}

export interface User {
  address?: User_Address;
  age?: number;
  email?: string;
  id: number;
  isActive: boolean;
  name: string;
  score?: number;
  tags: string[] | any[];
}

