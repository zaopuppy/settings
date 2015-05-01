(custom-set-variables
 ;; custom-set-variables was added by Custom.
 ;; If you edit it by hand, you could mess it up, so be careful.
 ;; Your init file should contain only one such instance.
 ;; If there is more than one, they won't work right.
 '(ansi-color-names-vector ["#2d3743" "#ff4242" "#74af68" "#dbdb95" "#34cae2" "#008b8b" "#00ede1" "#e1e1e0"])
 '(custom-enabled-themes (quote (misterioso))))
(custom-set-faces
 ;; custom-set-faces was added by Custom.
 ;; If you edit it by hand, you could mess it up, so be careful.
 ;; Your init file should contain only one such instance.
 ;; If there is more than one, they won't work right.
 )


(setq-default scroll-conservatively 100000
	      scroll-up-aggressively 1
	      scroll-down-aggressively 1)

;; disable splash screen
(setq inhibit-startup-message t)

;; remove menu bar and tool bar
(menu-bar-mode -1)
;; (tool-bar-mode -1)

;; force use spaces instead of tab
(setq-default indent-tabs-mode nil)

(setq make-backup-files nil)



;; END
