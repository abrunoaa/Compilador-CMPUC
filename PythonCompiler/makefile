executavel: objeto
	@gcc -O2 -no-pie output.o -o output
	@objdump -S --disassemble output > output.dump

assembly: input.toy main.py parser.py lexer.py codegen.py ast.py
	@python3 main.py

objeto: assembly output.ll
	@llc -filetype=obj output.ll

clean:
	@rm -f -r __pycache__
	@rm -f output.ll output.o output.dump output
