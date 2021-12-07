function main(abilityData)
	local effect = import("$.potion.PotionEffectType")
	
	plugin.registerEvent(abilityData, "PlayerInteractEvent", 0, function(a, e)
		if e:getAction():toString() == "RIGHT_CLICK_AIR" or e:getAction():toString() == "RIGHT_CLICK_BLOCK" then
			local arrow = {newInstance("$.inventory.ItemStack", { import("$.Material").ARROW, 1 }) }
			if e:getItem() ~= nil then
				if e:getItem():getType():toString() == "BOW" and e:getPlayer():getInventory():containsAtLeast(arrow[1], 1) == false then
					if game.checkCooldown(e:getPlayer(), a, 0) then
						e:getPlayer():getInventory():addItem(arrow)
					end
				end
			end
		end
	end)
	
	plugin.registerEvent(abilityData, "EntityDamageByEntityEvent", 400, function(a, e)
		if e:getDamager():getType():toString() == "PLAYER" and e:getEntity():getType():toString() == "PLAYER" then
			if math.random(10) <= 2 then
				if game.checkCooldown(e:getDamager(), a, 1) then
					e:getEntity():addPotionEffect(newInstance("$.potion.PotionEffect", {effect.WITHER, 200, 0}))
				end
			end
		end
	end)
	
	plugin.registerEvent(abilityData, "EntityTargetLivingEntityEvent", 0, function(a, e)
		if e:getTarget() ~= nil and e:getEntity() ~= nil then
			if e:getTarget():getType():toString() == "PLAYER" and e:getEntity():getType():toString() == "SKELETON" then
				if game.checkCooldown(e:getTarget(), a, 2) then
					e:setTarget(nil)
					e:setCancelled(true)
				end
			end
		end
	end)
end