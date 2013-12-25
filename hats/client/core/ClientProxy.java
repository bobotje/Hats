package hats.client.core;

import hats.api.RenderOnEntityHelper;
import hats.client.gui.GuiHatSelection;
import hats.client.model.ModelHat;
import hats.client.render.RenderHat;
import hats.client.render.helper.HelperBlaze;
import hats.client.render.helper.HelperChicken;
import hats.client.render.helper.HelperCow;
import hats.client.render.helper.HelperCreeper;
import hats.client.render.helper.HelperEnderman;
import hats.client.render.helper.HelperGhast;
import hats.client.render.helper.HelperOcelot;
import hats.client.render.helper.HelperPig;
import hats.client.render.helper.HelperPlayer;
import hats.client.render.helper.HelperSheep;
import hats.client.render.helper.HelperSkeleton;
import hats.client.render.helper.HelperSlime;
import hats.client.render.helper.HelperSpider;
import hats.client.render.helper.HelperSquid;
import hats.client.render.helper.HelperVillager;
import hats.client.render.helper.HelperWolf;
import hats.client.render.helper.HelperZombie;
import hats.common.core.CommonProxy;
import hats.common.core.HatHandler;
import hats.common.entity.EntityHat;
import hats.common.thread.ThreadReadHats;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;

import org.w3c.dom.Document;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

public class ClientProxy extends CommonProxy 
{

	@Override
	public void initRenderersAndTextures() 
	{
		RenderingRegistry.registerEntityRenderingHandler(EntityHat.class, new RenderHat());
		
		renderHelpers.put(EntityBlaze.class		, new HelperBlaze());
		renderHelpers.put(EntityChicken.class	, new HelperChicken());
		renderHelpers.put(EntityCow.class		, new HelperCow());
		renderHelpers.put(EntityCreeper.class	, new HelperCreeper());
		renderHelpers.put(EntityEnderman.class	, new HelperEnderman());
		renderHelpers.put(EntityGhast.class		, new HelperGhast());
		renderHelpers.put(EntityOcelot.class	, new HelperOcelot());
		renderHelpers.put(EntityPig.class		, new HelperPig());
		renderHelpers.put(EntityPlayer.class	, new HelperPlayer());
		renderHelpers.put(EntitySheep.class		, new HelperSheep());
		renderHelpers.put(EntitySkeleton.class	, new HelperSkeleton());
		renderHelpers.put(EntitySlime.class		, new HelperSlime());
		renderHelpers.put(EntitySpider.class	, new HelperSpider());
		renderHelpers.put(EntitySquid.class		, new HelperSquid());
		renderHelpers.put(EntityVillager.class	, new HelperVillager());
		renderHelpers.put(EntityWolf.class		, new HelperWolf());
		renderHelpers.put(EntityZombie.class	, new HelperZombie());
	}

	@Override
	public void initTickHandlers() 
	{
		super.initTickHandlers();
		tickHandlerClient = new TickHandlerClient();
		TickRegistry.registerTickHandler(tickHandlerClient, Side.CLIENT);
	}
	
	@Override
	public void getHatsAndOpenGui()
	{
		((Thread)new ThreadReadHats(HatHandler.hatsFolder, this, true)).start();
	}

	@Override
	public void clearAllHats()
	{
		super.clearAllHats();
		models.clear();
		bufferedImages.clear();
		bufferedImageID.clear();
	}
	
	@Override
	public void remap(String duplicate, String original)
	{
		super.remap(duplicate, original);
		models.put(duplicate, models.get(original));
		bufferedImages.put(duplicate, bufferedImages.get(original));
	}
	
	@Override
	public void openHatsGui()
	{
		FMLClientHandler.instance().displayGuiScreen(Minecraft.getMinecraft().thePlayer, new GuiHatSelection(Minecraft.getMinecraft().thePlayer));
	}

	@Override
	public void loadHatFile(File file)
	{
		super.loadHatFile(file);
		
		String hatName = file.getName().substring(0, file.getName().length() - 4).toLowerCase();
		
		try
		{
			ZipFile zipFile = new ZipFile(file);
			Enumeration entries = zipFile.entries();
			
			while(entries.hasMoreElements())
			{
				ZipEntry entry = (ZipEntry)entries.nextElement();
				if(!entry.isDirectory())
				{
					if(entry.getName().endsWith(".png"))
					{
						InputStream stream = zipFile.getInputStream(entry);
						BufferedImage image = ImageIO.read(stream);
						
						bufferedImages.put(hatName, image);
						bufferedImageID.put(image, -1);
						
					}
					if(entry.getName().endsWith(".xml"))
					{
						InputStream stream = zipFile.getInputStream(entry);
						
						DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
						
						Document doc = builder.parse(stream);
						
						models.put(hatName, new ModelHat(doc));	
					}
				}
			}
			
			zipFile.close();
		}
		catch (Exception e1) 
		{
			//an exception would have been thrown before this in the thread, so no output.
		}
	}
	
	public static HashMap<String, BufferedImage> bufferedImages = new HashMap<String, BufferedImage>();
	public static HashMap<BufferedImage, Integer> bufferedImageID = new HashMap<BufferedImage, Integer>();
	public static HashMap<String, ModelHat> models = new HashMap<String, ModelHat>();
	public static HashMap<Class, RenderOnEntityHelper> renderHelpers = new HashMap<Class, RenderOnEntityHelper>();
	
}
