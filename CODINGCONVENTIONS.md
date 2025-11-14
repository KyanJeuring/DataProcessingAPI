## Brace style (curly brackets)

- Note: this repository intentionally deviates from the NHL Stenden brace convention. Use the opening brace on the same line as the function or control statement. Examples:

	Preferred (this repo):

	```js
	function test() {
		return;
	}
	```

	NHL Stenden style (not used here):

	```js
	function test
	{
		return;
	}
	```

- Apply the same same-line-brace rule for `if`, `for`, `while`, and other blocks:

	```js
	if (condition) {
		doSomething();
	}
	```