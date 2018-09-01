/*
 * Pinata plugin - spawn pinata mob and kill it to get drops
 * Copyright (C)2018 Plajer
 *
 *  This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package pl.plajer.pinata.handlers.language;

import java.util.Arrays;
import java.util.List;

/**
 * @author Plajer
 * <p>
 * Created at 01.09.2018
 */
public enum Locale {

  ENGLISH("English", "en_GB", "Plajer", Arrays.asList("default", "english", "en")),
  GERMAN("Deutsch", "de_DE", "Tigerkatze and POEditor contributors", Arrays.asList("deutsch", "german", "de")),
  HUNGARIAN("Magyar", "hu_HU", "POEditor contributors (montlikadani)", Arrays.asList("hungarian", "magyar", "hu")),
  POLISH("Polski", "pl_PL", "Plajer", Arrays.asList("polish", "polski", "pl"));

  String formattedName;
  String prefix;
  String author;
  List<String> aliases;

  Locale(String formattedName, String prefix, String author, List<String> aliases) {
    this.prefix = prefix;
    this.formattedName = formattedName;
    this.author = author;
    this.aliases = aliases;
  }

  public String getFormattedName() {
    return formattedName;
  }

  public String getAuthor() {
    return author;
  }

  public String getPrefix() {
    return prefix;
  }

  public List<String> getAliases() {
    return aliases;
  }

}
