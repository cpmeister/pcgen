/*
 * Copyright (c) 2009 Tom Parker <thpr@users.sourceforge.net>
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA
 */
package pcgen.gui.converter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Set;

import pcgen.base.util.DoubleKeyMapToList;
import pcgen.cdom.base.CDOMObject;
import pcgen.cdom.enumeration.ListKey;
import pcgen.core.Ability;
import pcgen.core.ArmorProf;
import pcgen.core.Campaign;
import pcgen.core.Deity;
import pcgen.core.Domain;
import pcgen.core.EquipmentModifier;
import pcgen.core.Language;
import pcgen.core.PCTemplate;
import pcgen.core.Race;
import pcgen.core.ShieldProf;
import pcgen.core.Skill;
import pcgen.core.WeaponProf;
import pcgen.core.character.CompanionMod;
import pcgen.core.spell.Spell;
import pcgen.gui.converter.loader.BasicLoader;
import pcgen.gui.converter.loader.ClassLoader;
import pcgen.gui.converter.loader.CopyLoader;
import pcgen.gui.converter.loader.EquipmentLoader;
import pcgen.gui.converter.loader.SelfCopyLoader;
import pcgen.persistence.PersistenceLayerException;
import pcgen.persistence.lst.AbilityCategoryLoader;
import pcgen.persistence.lst.CampaignSourceEntry;
import pcgen.persistence.lst.LstFileLoader;
import pcgen.rules.context.EditorLoadContext;
import pcgen.system.LanguageBundle;
import pcgen.util.Logging;

public class LSTConverter extends Observable
{
	private final AbilityCategoryLoader catLoader = new AbilityCategoryLoader();
	private final EditorLoadContext context;
	private List<Loader> loaders;
	private Set<URI> written = new HashSet<URI>();
	private final String outDir;
	private final File rootDir;
	private final DoubleKeyMapToList<Loader, URI, CDOMObject> injected = new DoubleKeyMapToList<Loader, URI, CDOMObject>();
	private final ConversionDecider decider;
	
	public LSTConverter(EditorLoadContext lc, File root, String outputDir,
			ConversionDecider cd)
	{
		context = lc;
		rootDir = root;
		outDir = outputDir;
		loaders = setupLoaders(context);
		decider = cd;
	}

	/**
	 * Return the number of files referred to by the campaign
	 * @param campaign The campaign to be tallied.
	 * @return The number of lst files used.
	 */
	public int getNumFilesInCampaign(Campaign campaign)
	{
		int numFiles = 0;
	
		for (final Loader loader : loaders)
		{
			List<CampaignSourceEntry> files = loader.getFiles(campaign);
			numFiles += files.size();
		}
		return numFiles;
	}
	
	/**
	 * Initialise the list of campaigns. This will load the ability 
	 * categories in advance of the conversion.
	 * @param campaigns The campaigns or sources to be converted.
	 */
	public void initCampaigns(List<Campaign> campaigns)
	{
		for (Campaign campaign : campaigns)
		{
			// load ability categories first as they used to only be at the game
			// mode
			try
			{
				catLoader.loadLstFiles(context, campaign
						.getSafeListFor(ListKey.FILE_ABILITY_CATEGORY));
			}
			catch (PersistenceLayerException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void processCampaign(Campaign campaign)
	{
		startItem(campaign);
	}

	private void startItem(final Campaign campaign)
	{
		for (final Loader loader : loaders)
		{
			List<CampaignSourceEntry> files = loader.getFiles(campaign);
			for (final CampaignSourceEntry cse : files)
			{
				final URI uri = cse.getURI();
				setChanged();
				notifyObservers(uri);
				if (written.contains(uri))
				{
					continue;
				}
				written.add(uri);
				if (!"file".equalsIgnoreCase(uri.getScheme()))
				{
					Logging.log(Logging.WARNING, "Skipping campaign " + uri + " as it is not a local file.");
					continue;
				}
				File in = new File(uri);
				File base = findSubRoot(rootDir, in);
				if (base == null)
				{
					Logging.log(Logging.WARNING, "Skipping campaign " + uri + " as it is not in the selected source directory.");
					continue;
				}
				String relative = in.toString().substring(
						base.toString().length() + 1);
				if (!in.exists())
				{
					Logging.log(Logging.WARNING, "Skipping campaign " + uri
						+ " as it does not exist. Campaign is "
						+ cse.getCampaign().getSourceURI());
					continue;
				}
				File outFile = new File(outDir, File.separator + relative);
				if (outFile.exists())
				{
					Logging.log(Logging.WARNING, "Won't overwrite: " + outFile);
					continue;
				}
				ensureParents(outFile.getParentFile());
				try
				{
					String result = load(uri, loader);
					if (result != null)
					{
						FileWriter fis = new FileWriter(outFile);
						fis.write(result);
						fis.close();
					}
				}
				catch (PersistenceLayerException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private List<Loader> setupLoaders(EditorLoadContext context)
	{
		List<Loader> loaderList = new ArrayList<Loader>();
		loaderList.add(new BasicLoader<WeaponProf>(context, WeaponProf.class,
				ListKey.FILE_WEAPON_PROF));
		loaderList.add(new BasicLoader<ArmorProf>(context, ArmorProf.class,
				ListKey.FILE_ARMOR_PROF));
		loaderList.add(new BasicLoader<ShieldProf>(context, ShieldProf.class,
				ListKey.FILE_SHIELD_PROF));
		loaderList.add(new BasicLoader<Skill>(context, Skill.class,
				ListKey.FILE_SKILL));
		loaderList.add(new BasicLoader<Language>(context, Language.class,
				ListKey.FILE_LANGUAGE));
		loaderList.add(new BasicLoader<Ability>(context, Ability.class,
				ListKey.FILE_FEAT));
		loaderList.add(new BasicLoader<Ability>(context, Ability.class,
				ListKey.FILE_ABILITY));
		loaderList.add(new BasicLoader<Race>(context, Race.class,
				ListKey.FILE_RACE));
		loaderList.add(new BasicLoader<Domain>(context, Domain.class,
				ListKey.FILE_DOMAIN));
		loaderList.add(new BasicLoader<Spell>(context, Spell.class,
				ListKey.FILE_SPELL));
		loaderList.add(new BasicLoader<Deity>(context, Deity.class,
				ListKey.FILE_DEITY));
		loaderList.add(new BasicLoader<PCTemplate>(context, PCTemplate.class,
				ListKey.FILE_TEMPLATE));
		loaderList.add(new EquipmentLoader(context, ListKey.FILE_EQUIP));
		loaderList.add(new BasicLoader<EquipmentModifier>(context,
				EquipmentModifier.class, ListKey.FILE_EQUIP_MOD));
		loaderList.add(new BasicLoader<CompanionMod>(context, CompanionMod.class,
				ListKey.FILE_COMPANION_MOD));
		loaderList.add(new ClassLoader(context));
		loaderList.add(new CopyLoader(ListKey.FILE_ABILITY_CATEGORY));
		loaderList.add(new CopyLoader(ListKey.LICENSE_FILE));
		loaderList.add(new CopyLoader(ListKey.FILE_KIT));
		loaderList.add(new CopyLoader(ListKey.FILE_BIO_SET));
		loaderList.add(new CopyLoader(ListKey.FILE_PCC));
		loaderList.add(new SelfCopyLoader());
		return loaderList;
	}

	private void ensureParents(File parentFile)
	{
		if (!parentFile.exists())
		{
			ensureParents(parentFile.getParentFile());
			parentFile.mkdir();
		}
	}

	private File findSubRoot(File root, File in)
	{
		File parent = in.getParentFile();
		if (parent == null)
		{
			return null;
		}
		if (parent.getAbsolutePath().equals(root.getAbsolutePath()))
		{
			return parent;
		}
		return findSubRoot(root, parent);
	}

	private String load(URI uri, Loader loader) throws InterruptedException,
			PersistenceLayerException
	{
		StringBuilder dataBuffer;
		context.setSourceURI(uri);
		context.setExtractURI(uri);
		try
		{
			dataBuffer = LstFileLoader.readFromURI(uri);
		}
		catch (PersistenceLayerException ple)
		{
			String message = LanguageBundle.getFormattedString(
					"Errors.LstFileLoader.LoadError", //$NON-NLS-1$
					uri, ple.getMessage());
			Logging.errorPrint(message);
			return null;
		}

		StringBuilder resultBuffer = new StringBuilder(dataBuffer.length());
		final String aString = dataBuffer.toString();

		String[] fileLines = aString.replaceAll("\r\n", "\r").split(
				LstFileLoader.LINE_SEPARATOR_REGEXP);
		for (int line = 0; line < fileLines.length; line++)
		{
			String lineString = fileLines[line];
			if ((lineString.length() == 0)
					|| (lineString.charAt(0) == LstFileLoader.LINE_COMMENT_CHAR)
					|| lineString.startsWith("SOURCE"))
			{
				resultBuffer.append(lineString);
			}
			else
			{
				List<CDOMObject> newObj = loader.process(resultBuffer, line,
						lineString, decider);
				if (newObj != null)
				{
					for (CDOMObject cdo : newObj)
					{
						injected.addToListFor(loader, uri, cdo);
					}
				}
			}
			resultBuffer.append("\n");
		}
		return resultBuffer.toString();
	}

	public Collection<Loader> getInjectedLoaders()
	{
		return injected.getKeySet();
	}

	public Collection<URI> getInjectedURIs(Loader l)
	{
		return injected.getSecondaryKeySet(l);
	}

	public Collection<CDOMObject> getInjectedObjects(Loader l, URI uri)
	{
		return injected.getListFor(l, uri);
	}
}
