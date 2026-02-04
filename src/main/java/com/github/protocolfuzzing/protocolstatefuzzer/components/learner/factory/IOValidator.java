package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.factory;

import de.learnlib.query.Query;
import de.learnlib.ralib.oracles.DataWordOracle;
import de.learnlib.ralib.words.InputSymbol;
import de.learnlib.ralib.words.OutputSymbol;
import de.learnlib.ralib.words.PSymbolInstance;
import de.learnlib.ralib.words.ParameterizedSymbol;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.word.Word;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.stream.Collectors;


/**
 * {@code DataWordOracle} that throws exceptions when encountering queries
 * with symbols not present in the provided alphabet.
 */
public class IOValidator implements DataWordOracle {

    private final DataWordOracle oracle;

    private final Collection<ParameterizedSymbol> inputs;

    private final Collection<ParameterizedSymbol> outputs;

    /**
    * Construct a new validator.
    *
    * @param oracle     the oracle to pass valid queries to
    * @param alphabet   the alphabet in which to check for symbols
 */
    public IOValidator(DataWordOracle oracle, Alphabet<? extends ParameterizedSymbol> alphabet) {
        this.oracle = oracle;
        this.inputs = alphabet.stream().filter(p -> p instanceof InputSymbol).collect(Collectors.toSet());
        this.outputs = alphabet.stream().filter(p -> p instanceof OutputSymbol).collect(Collectors.toSet());
    }

    @Override
    public void processQueries(Collection<? extends Query<PSymbolInstance, Boolean>> queries) {
        for (Query<PSymbolInstance, Boolean> query : queries){
            validate(query.getInput());
        }
        oracle.processQueries(queries);
    }

    private void validate(Word<PSymbolInstance> query) {
        boolean shouldBeInputSymbol = true;
        for (PSymbolInstance symbol : query) {
            if (shouldBeInputSymbol && !inputs.contains(symbol.getBaseSymbol())){
                throw new AlphabetMissingSymbolException(symbol.getBaseSymbol());
            } else if (!shouldBeInputSymbol && !outputs.contains(symbol.getBaseSymbol())) {
                throw new AlphabetMissingSymbolException(symbol.getBaseSymbol());
            }
            shouldBeInputSymbol = !shouldBeInputSymbol;
        }
    }

    @SuppressWarnings("serial")
    private static class AlphabetMissingSymbolException extends RuntimeException {

        private AlphabetMissingSymbolException(ParameterizedSymbol symbol) {
                super(String.format("Alphabet is missing symbol \"%s\"", symbol.toString()));
            }

        private void writeObject(ObjectOutputStream oos) throws IOException {
            throw new IOException("This class is NOT serializable.");
        }
    }
}
