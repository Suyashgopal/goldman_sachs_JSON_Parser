# JsonToTsCompiler

Minimal Java CLI tool that converts JSON arrays into TypeScript declaration files.

## Features

- Zero dependencies
- Automatic TypeScript type inference
- Optional field detection
- Nested object support
- Union type generation
- Deterministic output

---

## Compile

```bash
javac com/typegen/*.java
```

## Run

```bash
java com.typegen.Main <input.json> <output.d.ts> <RootType>
```

### Example

```bash
java com.typegen.Main sample.json output.d.ts User
```

---

## Example

### Input

```json
[
  {
    "id": 1,
    "name": "Alice",
    "tags": ["java", "backend"]
  },
  {
    "id": 2,
    "name": "Bob"
  }
]
```

### Output

```typescript
export interface User {
  id: number;
  name: string;
  tags?: string[];
}
```

---

## Type Mapping

| JSON | TypeScript |
|------|-------------|
| string | string |
| number | number |
| boolean | boolean |
| null | null |
| array | T[] |
| object | interface |

---

## Limitations

- Root JSON must be an array of objects
- Empty arrays become `any[]`
- Mixed arrays generate union types

---

## Project Structure

```text
com/typegen/
├── Main.java
├── JsonParser.java
└── TypeEngine.java
```
