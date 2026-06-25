package xyz.srnyx.annoyingapi.message;

import xyz.srnyx.annoyingapi.parents.Registrable;

import java.util.function.Consumer;
import java.util.function.Supplier;


public abstract class MessagesProvider extends Registrable implements Consumer<AnnoyingMessages>, Supplier<AnnoyingMessages> {}
