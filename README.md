# JsonToTsCompiler

A minimal, zero-dependency Java CLI tool that converts JSON arrays to TypeScript declaration files.

## Features

- **Zero Dependencies**: Uses only Java SE standard library
- **Type Inference**: Automatically infers TypeScript types from JSON data
- **Optional Fields**: Marks fields as optional (?) when missing in some objects
- **Nested Objects**: Handles deeply nested structures with named interfaces
- **Type Merging**: Combines different types into union types
- **Deterministic Output**: Alphabetically sorted keys for consistent results

## Build

```bash
javac com/typegen/*.java
```

## Usage

```bash
java com.typegen.Main <input.json> <output.d.ts> <RootTypeName>
```

### Example

```bash
java com.typegen.Main sample.json output.d.ts User
```

This will read `sample.json` and generate `output.d.ts` with a root interface named `User`.

## Sample Input (sample.json)

```json
[
  {
    "id": 1,
    "name": "Alice",
    "email": "alice@example.com",
    "age": 30,
    "isActive": true,
    "address": {
      "street": "123 Main St",
      "city": "Boston",
      "zipCode": "02101"
    },
    "tags": ["developer", "java"]
  },
  {
    "id": 2,
    "name": "Bob",
    "age": 25,
    "isActive": false,
    "address": {
      "street": "456 Oak Ave",
      "city": "Seattle"
    },
    "tags": ["designer"],
    "score": 95.5
  }
]
```

## Sample Output (output.d.ts)

```typescript
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
  tags: string[];
}
```

## Architecture

### Main.java
- Handles CLI arguments
- Reads input JSON file
- Writes output TypeScript file
- Error handling and user feedback

### JsonParser.java
- Recursive-descent JSON parser
- Converts JSON to Java Collections (Map, List, primitives)
- Handles escape sequences and Unicode
- Robust error messages with position tracking

### TypeEngine.java
- Analyzes object structures across multiple instances
- Tracks field occurrences to determine optional fields
- Merges types into union types when needed
- Generates clean, sorted TypeScript interfaces

## Type Mapping

| JSON Type | TypeScript Type |
|-----------|----------------|
| String | string |
| Number | number |
| Boolean | boolean |
| Null | null |
| Array | T[] |
| Object | Named Interface |

## Limitations

- Arrays with mixed types create union types: `(string | number)[]`
- Empty arrays default to `any[]`
- No support for tuples or advanced TypeScript features
- Requires input to be an array of objects at the root level
