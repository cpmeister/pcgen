/*
 *  Initiative - A role playing utility to track turns
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package gmgen.plugin.dice;

import java.util.stream.IntStream;

/**
 * If the original value was 1, produces -9
 * If the original value was 20, produces 30
 * Otherwise produces results unchanged
 */
class SystemModifier implements ResultModifier
{
	private static int systemExplode(final int i) {
		switch (i) {
			case 1:
				return -9;
			case 20:
				return 30;
			default:
				return i;
		}
	}

	@Override
	public IntStream apply(final IntStream in)
	{
		return in.map(SystemModifier::systemExplode);
	}
}
