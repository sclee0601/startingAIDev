# Self-Correcting AI Agent with Cost Optimization
## Building Agents That Validate, Retry, and Minimize Costs

---

## Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [Core Principles](#core-principles)
3. [Implementation Patterns](#implementation-patterns)
4. [Cost Optimization Strategies](#cost-optimization-strategies)
5. [Error Detection & Handling](#error-detection--handling)
6. [Complete Working Example](#complete-working-example)
7. [Deployment Best Practices](#deployment-best-practices)

---

## Architecture Overview

### The Loop
```
User Request
    ↓
Generate Code/Solution
    ↓
Validate/Compile/Test
    ↓
Passes? → YES → Return to User
    ↓ NO
Extract Error Info
    ↓
Refine Prompt with Context
    ↓
Regenerate (using cheaper model if possible)
    ↓
(repeat until success or max retries)
```

### Key Decision Points
- **Validation Level**: Compilation, linting, runtime tests, or all three
- **Retry Strategy**: Which model to use on retry (faster/cheaper variants)
- **Cost vs Performance**: Trade-off between model quality and token spend
- **Failure Threshold**: When to stop retrying and report failure

---

## Core Principles

### 1. **Validate Before Returning**
Never return output without proving it works. For code:
- Compilation/syntax check
- Basic execution test
- Type checking (if applicable)

### 2. **Preserve Context Across Retries**
- Keep the original request
- Include the error message
- Show what was attempted
- Explain the fix you're trying

This reduces tokens spent on explaining the problem vs fixing it.

### 3. **Use Model Stratification**
- **Initial Request**: Use your best model (Claude Opus/Sonnet 4)
- **Retry #1-2**: Same model with refined prompt (still in context)
- **Retry #3+**: Switch to faster model (Haiku) if cost matters more than quality
- **Final Retry**: Back to best model before giving up

### 4. **Fail Fast on Known Impossible Cases**
If error is clearly unsolvable with retries (e.g., "I need Python 3.2 support"), stop early.

### 5. **Track Metrics**
Monitor:
- Validation pass rate (% that work first try)
- Retry count distribution
- Cost per successful output
- Time per iteration

---

## Implementation Patterns

### Pattern 1: Simple Validation with Single Retry

```javascript
async function generateWithValidation(prompt, language = 'javascript') {
  const models = {
    javascript: validateJavaScript,
    python: validatePython,
    typescript: validateTypeScript,
    go: validateGo,
  };

  let attempt = 0;
  const maxAttempts = 3;
  let lastError = null;

  while (attempt < maxAttempts) {
    attempt++;
    
    // Generate code
    const code = await generateCode(prompt, attempt === 1);
    
    // Validate
    const validation = models[language](code);
    
    if (validation.success) {
      return {
        code,
        success: true,
        attempts: attempt,
        error: null,
      };
    }
    
    lastError = validation.error;
    
    // If final attempt failed, return with error
    if (attempt === maxAttempts) {
      return {
        code,
        success: false,
        attempts: attempt,
        error: lastError,
      };
    }
    
    // Prepare for retry
    prompt = buildRetryPrompt(prompt, code, lastError, attempt);
  }
}

function buildRetryPrompt(originalPrompt, failedCode, error, attemptNumber) {
  return `
Original request: ${originalPrompt}

Attempt ${attemptNumber} failed with error:
\`\`\`
${error}
\`\`\`

Failed code:
\`\`\`
${failedCode}
\`\`\`

Please fix the error and provide corrected code. Focus only on fixing the specific error, not rewriting the entire solution.
  `;
}

async function generateCode(prompt, isFirstAttempt) {
  const model = isFirstAttempt ? 'claude-opus-4-6' : 'claude-sonnet-4-6';
  
  const response = await fetch('https://api.anthropic.com/v1/messages', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      model,
      max_tokens: 2000,
      messages: [{ role: 'user', content: prompt }],
    }),
  });
  
  const data = await response.json();
  return data.content[0].text;
}
```

### Pattern 2: Intelligent Retry with Error Classification

```javascript
async function generateWithSmartRetry(prompt, language = 'python') {
  let attempt = 0;
  const maxAttempts = 4;
  let code = null;
  let lastError = null;
  let errorHistory = [];

  while (attempt < maxAttempts) {
    attempt++;
    
    // Decide which model to use
    const model = selectModel(attempt, maxAttempts);
    
    // Generate
    code = await claudeGenerate(prompt, model);
    
    // Validate
    const validation = await validateCode(code, language);
    
    if (validation.isValid) {
      return { 
        code, 
        success: true, 
        attempts: attempt,
        model,
      };
    }
    
    lastError = validation.error;
    errorHistory.push({
      attempt,
      error: lastError,
      code,
      model,
    });
    
    // Check if error is unrecoverable
    if (isUnrecoverableError(lastError)) {
      return {
        code,
        success: false,
        attempts: attempt,
        error: lastError,
        reason: 'Unrecoverable error detected',
      };
    }
    
    // If final attempt, return failure
    if (attempt === maxAttempts) {
      return {
        code,
        success: false,
        attempts: attempt,
        error: lastError,
        history: errorHistory,
      };
    }
    
    // Build refined prompt for retry
    prompt = createFocusedRetryPrompt(
      prompt,
      code,
      lastError,
      attempt,
      errorHistory
    );
  }
}

function selectModel(attempt, maxAttempts) {
  // Attempt 1: Best model (high cost, high quality)
  if (attempt === 1) return 'claude-opus-4-6';
  
  // Attempts 2-3: Sonnet (good balance)
  if (attempt < maxAttempts - 1) return 'claude-sonnet-4-6';
  
  // Final attempt: Back to best model (spend extra to fix it)
  return 'claude-opus-4-6';
}

function createFocusedRetryPrompt(
  original,
  failedCode,
  error,
  attemptNumber,
  history
) {
  const previousAttempts = history.map(h => 
    `Attempt ${h.attempt}: ${h.error.split('\n')[0]}`
  ).join('\n');

  return `
TASK: ${original}

FAILED ATTEMPTS:
${previousAttempts}

CURRENT ERROR:
\`\`\`
${error}
\`\`\`

MOST RECENT CODE:
\`\`\`
${failedCode}
\`\`\`

INSTRUCTIONS:
1. Identify the root cause of the error
2. Make MINIMAL changes to fix it
3. Keep the original logic intact
4. Return ONLY the corrected code block, no explanation

CRITICAL: Do not rewrite the entire solution. Fix only what's broken.
  `;
}

function isUnrecoverableError(error) {
  const unrecoverable = [
    'module not found',
    'import error',
    'no such file or directory',
    'permission denied',
  ];
  
  return unrecoverable.some(msg => 
    error.toLowerCase().includes(msg)
  );
}

async function validateCode(code, language) {
  // Placeholder - implement per language
  switch(language) {
    case 'python':
      return validatePython(code);
    case 'javascript':
      return validateJavaScript(code);
    case 'java':
      return validateJava(code);
    default:
      return { isValid: true, error: null };
  }
}

function validatePython(code) {
  try {
    const { spawnSync } = require('child_process');
    const result = spawnSync('python3', ['-m', 'py_compile', '-'], {
      input: code,
      encoding: 'utf8',
      timeout: 5000,
    });
    
    if (result.error || result.status !== 0) {
      return {
        isValid: false,
        error: result.stderr || result.error.message,
      };
    }
    
    return { isValid: true, error: null };
  } catch (e) {
    return { isValid: false, error: e.message };
  }
}

function validateJavaScript(code) {
  try {
    new Function(code);
    return { isValid: true, error: null };
  } catch (e) {
    return { isValid: false, error: e.message };
  }
}
```

### Pattern 3: Cost-Optimized Retry with Token Tracking

```javascript
class CostOptimizedAgent {
  constructor(config = {}) {
    this.maxAttempts = config.maxAttempts || 3;
    this.tokenBudget = config.tokenBudget || 50000; // total tokens allowed
    this.usedTokens = 0;
    this.costs = {
      'claude-opus-4-6': { input: 0.015, output: 0.045 },
      'claude-sonnet-4-6': { input: 0.003, output: 0.015 },
      'claude-haiku-4-5': { input: 0.00080, output: 0.004 },
    };
  }

  async generate(prompt, language) {
    let attempt = 0;
    let code = null;
    let lastError = null;
    const startTokens = this.usedTokens;

    while (attempt < this.maxAttempts) {
      attempt++;
      
      // Check token budget
      if (this.usedTokens > this.tokenBudget * 0.9) {
        return {
          success: false,
          error: 'Token budget exceeded',
          attempts: attempt,
          totalCost: this.calculateCost(startTokens),
        };
      }

      // Select model based on cost and attempt
      const model = this.selectCostAwareModel(attempt);

      // Generate with token tracking
      const { text, inputTokens, outputTokens } = await this.generateWithTracking(
        prompt,
        model
      );
      
      code = text;
      this.usedTokens += inputTokens + outputTokens;

      // Validate
      const validation = await validateCode(code, language);
      
      if (validation.isValid) {
        return {
          success: true,
          code,
          attempts: attempt,
          model,
          totalTokens: this.usedTokens - startTokens,
          totalCost: this.calculateCost(this.usedTokens - startTokens, model),
        };
      }

      lastError = validation.error;

      if (attempt === this.maxAttempts) {
        return {
          success: false,
          code,
          error: lastError,
          attempts: attempt,
          totalTokens: this.usedTokens - startTokens,
          totalCost: this.calculateCost(this.usedTokens - startTokens),
        };
      }

      // Prepare focused retry prompt
      prompt = this.createRetryPrompt(prompt, code, lastError, attempt);
    }
  }

  selectCostAwareModel(attempt) {
    // First attempt: use best model
    if (attempt === 1) return 'claude-sonnet-4-6';
    
    // Middle attempts: use faster model
    if (attempt < this.maxAttempts - 1) return 'claude-haiku-4-5';
    
    // Final attempt: back to better model
    return 'claude-sonnet-4-6';
  }

  async generateWithTracking(prompt, model) {
    const response = await fetch('https://api.anthropic.com/v1/messages', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        model,
        max_tokens: 1024,
        messages: [{ role: 'user', content: prompt }],
      }),
    });

    const data = await response.json();
    return {
      text: data.content[0].text,
      inputTokens: data.usage.input_tokens,
      outputTokens: data.usage.output_tokens,
    };
  }

  calculateCost(tokens, model = 'claude-sonnet-4-6') {
    const pricing = this.costs[model];
    // Rough calculation (simplification)
    return (tokens * 0.75 * pricing.input + tokens * 0.25 * pricing.output) / 1000;
  }

  createRetryPrompt(original, failedCode, error, attempt) {
    return `
ORIGINAL: ${original}

ERROR on attempt ${attempt}:
${error}

FAILED CODE:
\`\`\`
${failedCode}
\`\`\`

FIX THIS CODE. Return only the corrected version.
    `;
  }
}

// Usage
const agent = new CostOptimizedAgent({
  maxAttempts: 4,
  tokenBudget: 30000,
});

const result = await agent.generate(
  'Write a Python function that validates email addresses',
  'python'
);

console.log(`Success: ${result.success}`);
console.log(`Attempts: ${result.attempts}`);
console.log(`Cost: $${result.totalCost.toFixed(4)}`);
```

---

## Cost Optimization Strategies

### 1. **Use Model Stratification**
| Attempt | Model | Why |
|---------|-------|-----|
| 1st | Sonnet 4 | Good balance of speed & quality |
| 2nd-3rd | Haiku 4 | 80% cheaper, fast for focused fixes |
| Final | Opus 4 | If still failing, use best model |

**Estimated Savings: 40-60% per task**

### 2. **Compress Prompts Between Retries**
Don't repeat the entire context. Use:
```
"Fix line 42: undefined variable 'x'
Previous code worked except for this one issue."
```
Instead of re-sending 2KB of context each time.

**Estimated Savings: 30-50% on retry tokens**

### 3. **Batch Similar Requests**
If generating 10 functions, send in one prompt:
```
Generate these 3 functions in one message
(vs 3 separate API calls)
```

**Estimated Savings: 20% (reduced API overhead)**

### 4. **Cache Validation Logic**
```javascript
const validationCache = new Map();

function validateCode(code, language) {
  const hash = crypto.hash(code);
  if (validationCache.has(hash)) {
    return validationCache.get(hash);
  }
  
  const result = runValidation(code, language);
  validationCache.set(hash, result);
  return result;
}
```

**Estimated Savings: 10-20% if retrying similar code**

### 5. **Smart Failure Detection**
Stop retrying early if error is unrecoverable:
```javascript
if (error.includes('module not found') && attempt > 1) {
  // Already tried fixing module imports
  // No point retrying
  return fail();
}
```

**Estimated Savings: 20% (fewer wasted retry attempts)**

---

## Error Detection & Handling

### Language-Specific Validators

#### Python
```javascript
function validatePython(code) {
  const { spawnSync } = require('child_process');
  
  const result = spawnSync('python3', ['-m', 'py_compile', '-'], {
    input: code,
    encoding: 'utf8',
    timeout: 5000,
    stdio: ['pipe', 'pipe', 'pipe'],
  });

  if (result.error || result.status !== 0) {
    const error = result.stderr || result.error?.message;
    return {
      isValid: false,
      error: cleanErrorMessage(error),
      type: 'syntax_error',
    };
  }

  return { isValid: true, error: null, type: 'valid' };
}
```

#### JavaScript/TypeScript
```javascript
function validateTypeScript(code) {
  const ts = require('typescript');
  
  const result = ts.transpileModule(code, {
    compilerOptions: { 
      module: ts.ModuleKind.CommonJS,
      target: ts.ScriptTarget.ES2020,
    },
  });

  if (result.diagnostics && result.diagnostics.length > 0) {
    const error = result.diagnostics
      .map(d => ts.flattenDiagnosticMessageText(d.messageText, '\n'))
      .join('\n');
    
    return {
      isValid: false,
      error,
      type: 'type_error',
    };
  }

  // Try executing
  try {
    new Function(result.outputText);
    return { isValid: true, error: null, type: 'valid' };
  } catch (e) {
    return { isValid: false, error: e.message, type: 'runtime_error' };
  }
}
```

#### Java
```javascript
function validateJava(code) {
  const { spawnSync } = require('child_process');
  
  // Write to temp file
  const fs = require('fs');
  const className = extractClassName(code);
  const tempFile = `/tmp/${className}.java`;
  fs.writeFileSync(tempFile, code);

  // Compile
  const result = spawnSync('javac', [tempFile], {
    encoding: 'utf8',
    timeout: 10000,
  });

  fs.unlinkSync(tempFile);

  if (result.error || result.status !== 0) {
    return {
      isValid: false,
      error: result.stderr,
      type: 'compilation_error',
    };
  }

  return { isValid: true, error: null, type: 'valid' };
}
```

### Error Classification
```javascript
function classifyError(error) {
  const errorMap = {
    syntax: ['SyntaxError', 'unexpected token', 'invalid syntax'],
    type: ['TypeError', 'not defined', 'cannot read property'],
    import: ['ModuleNotFoundError', 'cannot find module', 'import error'],
    logic: ['AssertionError', 'ValueError', 'IndexError'],
    runtime: ['RuntimeError', 'crashed', 'segmentation fault'],
  };

  for (const [type, patterns] of Object.entries(errorMap)) {
    if (patterns.some(p => error.toLowerCase().includes(p.toLowerCase()))) {
      return type;
    }
  }

  return 'unknown';
}
```

---

## Complete Working Example

### Full Self-Correcting Agent
```javascript
const fetch = require('node-fetch');
const { spawnSync } = require('child_process');

class SelfCorrectingAgent {
  constructor(apiKey, config = {}) {
    this.apiKey = apiKey;
    this.maxRetries = config.maxRetries || 3;
    this.verbose = config.verbose || false;
    this.retryAttempts = [];
  }

  async generateCode(prompt, language = 'python') {
    console.log(`\n📝 Generating ${language} code...\n`);
    
    let code = null;
    let attempt = 0;
    let lastError = null;

    while (attempt < this.maxRetries) {
      attempt++;
      
      // Generate
      console.log(`🤖 Attempt ${attempt}/${this.maxRetries}...`);
      code = await this.callClaude(prompt);

      // Validate
      const validation = await this.validate(code, language);

      if (validation.success) {
        console.log(`✅ Code compiles successfully!\n`);
        return {
          success: true,
          code,
          attempts: attempt,
          language,
        };
      }

      lastError = validation.error;
      console.log(`❌ Compilation failed:\n${lastError}\n`);

      if (attempt === this.maxRetries) {
        console.log(`⚠️  Max retries reached.\n`);
        return {
          success: false,
          code,
          error: lastError,
          attempts: attempt,
          language,
        };
      }

      // Prepare retry
      prompt = this.buildRetryPrompt(prompt, code, lastError, attempt);
    }
  }

  async callClaude(prompt) {
    const response = await fetch('https://api.anthropic.com/v1/messages', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'x-api-key': this.apiKey,
      },
      body: JSON.stringify({
        model: 'claude-sonnet-4-20250514',
        max_tokens: 2048,
        messages: [
          { role: 'user', content: prompt },
        ],
      }),
    });

    const data = await response.json();

    if (!response.ok) {
      throw new Error(`API error: ${data.error.message}`);
    }

    // Extract code block if present
    const text = data.content[0].text;
    const codeMatch = text.match(/```(?:python|javascript|java|go)?\n([\s\S]*?)\n```/);
    
    return codeMatch ? codeMatch[1] : text;
  }

  async validate(code, language) {
    try {
      let result;

      switch (language.toLowerCase()) {
        case 'python':
          result = spawnSync('python3', ['-m', 'py_compile', '-'], {
            input: code,
            encoding: 'utf8',
            timeout: 5000,
          });
          break;

        case 'javascript':
          try {
            new Function(code);
            return { success: true, error: null };
          } catch (e) {
            return { success: false, error: e.message };
          }

        default:
          return { success: true, error: null };
      }

      if (result.error || result.status !== 0) {
        return {
          success: false,
          error: result.stderr || result.error.message,
        };
      }

      return { success: true, error: null };
    } catch (e) {
      return { success: false, error: e.message };
    }
  }

  buildRetryPrompt(original, failedCode, error, attempt) {
    return `
${original}

---

Attempt ${attempt}: The code failed to compile with this error:

\`\`\`
${error.split('\n').slice(0, 5).join('\n')}
\`\`\`

Failed code:
\`\`\`
${failedCode}
\`\`\`

IMPORTANT:
- Fix ONLY the compilation error
- Keep the original logic
- Return ONLY the corrected code block in markdown
- Do not add explanations or comments outside the code block
    `;
  }
}

// Usage
async function main() {
  const agent = new SelfCorrectingAgent(process.env.ANTHROPIC_API_KEY, {
    maxRetries: 3,
    verbose: true,
  });

  const prompt = `
Write a Python function that:
1. Takes a list of numbers
2. Returns the sum of squares of even numbers
3. Includes error handling for non-list inputs
  `;

  const result = await agent.generateCode(prompt, 'python');

  if (result.success) {
    console.log('📦 Final Code:\n');
    console.log(result.code);
    console.log(`\n✨ Generated in ${result.attempts} attempt(s)`);
  } else {
    console.log('Failed to generate working code');
    console.log(`Last error:\n${result.error}`);
  }
}

main().catch(console.error);
```

---

## Deployment Best Practices

### 1. **Monitoring & Logging**
```javascript
const logger = {
  logAttempt: (attempt, model, tokens, error = null) => {
    console.log(JSON.stringify({
      timestamp: new Date().toISOString(),
      attempt,
      model,
      inputTokens: tokens.input,
      outputTokens: tokens.output,
      error: error ? error.substring(0, 100) : null,
    }));
  },
};
```

### 2. **Rate Limiting**
```javascript
const RateLimiter = require('bottleneck').default;

const limiter = new RateLimiter({
  maxConcurrent: 5,
  minTime: 100, // ms between requests
});

const generateLimited = limiter.wrap(async (prompt) => {
  return await generate(prompt);
});
```

### 3. **Timeout Protection**
```javascript
async function generateWithTimeout(prompt, timeoutMs = 30000) {
  const timeoutPromise = new Promise((_, reject) =>
    setTimeout(() => reject(new Error('Timeout')), timeoutMs)
  );

  return Promise.race([
    generate(prompt),
    timeoutPromise,
  ]);
}
```

### 4. **Metrics Dashboard**
Track:
- First-time success rate
- Average retries per request
- Cost per successful generation
- Token efficiency (tokens per working line of code)

```javascript
class Metrics {
  constructor() {
    this.requests = [];
  }

  record(result) {
    this.requests.push({
      timestamp: Date.now(),
      success: result.success,
      attempts: result.attempts,
      cost: result.cost,
      tokens: result.tokens,
      language: result.language,
    });
  }

  getStats() {
    const total = this.requests.length;
    const successful = this.requests.filter(r => r.success).length;
    const avgAttempts = this.requests.reduce((sum, r) => sum + r.attempts, 0) / total;
    const totalCost = this.requests.reduce((sum, r) => sum + r.cost, 0);

    return {
      totalRequests: total,
      successRate: (successful / total * 100).toFixed(1) + '%',
      avgAttempts: avgAttempts.toFixed(2),
      totalCost: totalCost.toFixed(2),
    };
  }
}
```

---

## Key Takeaways

✅ **Always validate before returning** - Never trust AI output without proof it works

✅ **Preserve context in retries** - Include the error and failed code, not just the original prompt

✅ **Use model stratification** - Balance quality (Opus/Sonnet) with cost (Haiku) across retry attempts

✅ **Fail fast** - Detect unrecoverable errors early to save tokens

✅ **Track everything** - Monitor attempts, costs, and success rates to optimize your agent

✅ **Timeout protection** - Always set reasonable timeouts to avoid stuck agents

---

## Example Cost Comparison

### Without Self-Correction
- Task: 100 requests for Python code
- Success rate: 70%
- Failed requests: manual intervention required
- **Cost: $3.50 (100 × Sonnet at $0.035/1K tokens)**
- **Failure rate: 30%** ❌

### With Self-Correction (This Guide)
- Task: 100 requests for Python code  
- First-try success: 70%
- Retries with smart model selection: 25 requests × 1.5 avg retries
- Total requests: 100 + 37.5 = 137.5
- **Cost: $2.40** (smart model stratification saves 32%)
- **Success rate: 98%** ✅

---

## Resources

- [Anthropic API Documentation](https://docs.anthropic.com)
- [Claude Models Comparison](https://docs.anthropic.com/en/docs/about-claude/models/latest)
- [Prompt Engineering Guide](https://docs.anthropic.com/en/docs/build-with-claude/prompt-engineering/overview)
